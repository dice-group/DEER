package org.aksw.deer;

import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.server.Server;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.CompiledExecutionGraph;
import org.aksw.faraday_cage.engine.FaradayCageContext;
import org.aksw.faraday_cage.engine.PluginFactory;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.commons.cli.*;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.json.JSONObject;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.topbraid.shacl.validation.sparql.AbstractSPARQLExecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Main class for DEER.
 */
public class DeerController {

  static {
    MDC.put("requestId","main");
  }

  private static final Logger logger = LoggerFactory.getLogger(DeerController.class);

  private static final String HELP_MSG = "deer [OPTION]... <config_file_or_uri>";

  private static final Integer DEFAULT_PORT = 8080;

  private static final Options OPTIONS = new Options()
    .addOption(Option.builder("h")
      .longOpt("help").desc("show help message").build())
    .addOption(Option.builder("l")
      .longOpt("list").desc("list available deer plugins").build())
    .addOption(Option.builder("E")
      .longOpt("explain").desc("enable detailed explanation of graph validation").build())
    .addOption(Option.builder("v")
      .longOpt("validation-graph").desc("if $plugin_id is provided, get SHACL validation graph for $plugin_id, else get the complete validation graph.")
      .hasArg().argName("plugin_id").optionalArg(true).build())
    .addOption(Option.builder("s")
      .longOpt("server").desc("launch server").build())
    .addOption(Option.builder("p")
      .longOpt("port").desc("set port for server to listen on")
      .hasArg().argName("port_number").type(Number.class).build())
    ;

  private static final PluginManager pluginManager = new DefaultPluginManager();

  static {
    File plugins = new File("./plugins/");
    if (plugins.exists() && plugins.isDirectory()) {
      pluginManager.loadPlugins();
      pluginManager.startPlugins();
    }
  }

  private static final FaradayCageContext executionContext = Deer.getExecutionContext(pluginManager);

  public static FaradayCageContext getExecutionContext() {
    return executionContext;
  }

  public static void main(String args[]) {
    CommandLine cl = parseCommandLine(args);
    if (cl.hasOption('h')) {
      printHelp();
    } else if (cl.hasOption('l')) {
      PrintStream out = System.out;
      out.println(EnrichmentOperator.class.getSimpleName() + ":");
      new PluginFactory<>(EnrichmentOperator.class, pluginManager, FCAGE.ExecutionNode)
        .listAvailable().forEach(out::println);
      out.println(ModelReader.class.getSimpleName() + ":");
      new PluginFactory<>(ModelReader.class, pluginManager, FCAGE.ExecutionNode)
        .listAvailable().forEach(out::println);
      out.println(ModelWriter.class.getSimpleName() + ":");
      new PluginFactory<>(ModelWriter.class, pluginManager, FCAGE.ExecutionNode)
        .listAvailable().forEach(out::println);
      out.println(DeerExecutionNodeWrapper.class.getSimpleName() + ":");
      new PluginFactory<>(DeerExecutionNodeWrapper.class, pluginManager, FCAGE.ExecutionNode)
        .listAvailable().forEach(out::println);
    }  else if (cl.hasOption('v')) {
      PrintStream out = System.out;
      String id = cl.getOptionValue('v',"");
      if (!id.isEmpty()) {
        if (id.startsWith("deer:")) {
          id = DEER.NS + id.substring(5);
        }
        executionContext.getValidationModelFor(ResourceFactory.createResource(id)).write(out, "TTL");
      } else {
        executionContext.getFullValidationModel().write(out, "TTL");
      }
    } else if (cl.hasOption('s')) {
      Object port = cl.hasOption('p') ? cl.getOptionObject('p') : DEFAULT_PORT;
      if (port == null) {
        exitWithError("Expected a number as argument for option: p");
      } else {
        runDeerServer(((Number) port).intValue());
      }
    } else if (cl.getArgList().size() == 0){
      exitWithError("Please specify a configuration file to use!");
    } else {
      if (cl.hasOption('E')) {
        AbstractSPARQLExecutor.createDetails = true;
      }
      runDeer(compileDeer(cl.getArgList().get(0)));
    }
  }

  private static CommandLine parseCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cl = null;
    try {
      cl = parser.parse(OPTIONS, args, false);
    } catch (ParseException e) {
      exitWithError(e.getMessage());
    }
    return cl;
  }

  private static void exitWithError(String errorMsg) {
    System.out.println(ansi().fg(RED).a("Error:\n\t" + errorMsg).reset());
    printHelp();
    System.exit(1);
  }

  private static void printHelp() {
    new HelpFormatter().printHelp(HELP_MSG, OPTIONS);
  }

  private static void runDeerServer(int port) {
    logger.info("Trying to start DEER server on 0.0.0.0:{} ...", port);
    Server.getInstance().run(port);
  }

  public static CompiledExecutionGraph compileDeer(String fileName, String runId) {
    logger.info("Trying to read DEER configuration from file {}...", fileName);
    try {
      Model configurationModel = ModelFactory.createDefaultModel();
      final long startTime = System.currentTimeMillis();
      configurationModel.read(fileName);
      logger.info("Loading {} is done in {}ms.", fileName, (System.currentTimeMillis() - startTime));
      return executionContext.compile(configurationModel, runId);
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTPException trying to load model from " + fileName, e);
    }
  }

  public static CompiledExecutionGraph compileDeer(String fileName) {
    logger.info("Trying to read DEER configuration from file {}...", fileName);
    try {
      Model configurationModel = ModelFactory.createDefaultModel();
      final long startTime = System.currentTimeMillis();
      configurationModel.read(fileName);
      logger.info("Loading {} is done in {}ms.", fileName, (System.currentTimeMillis() - startTime));
      return executionContext.compile(configurationModel);
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTPException trying to load model from " + fileName, e);
    }
  }

  private static void runDeer(CompiledExecutionGraph compiledExecutionGraph) {
    compiledExecutionGraph.andThen(() -> writeAnalytics(Paths.get("deer-analytics.json").toAbsolutePath()));
    executionContext.run(compiledExecutionGraph);
  }

  public static void writeAnalytics(Path analyticsFile) {
    try {
      logger.info("Trying to write analytics data to " + analyticsFile);
      BufferedWriter writer = Files.newBufferedWriter(analyticsFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
      JSONObject analyticsForJob = DeerAnalyticsStore.getAnalyticsForJob(FaradayCageContext.getRunId());
      analyticsForJob.write(writer, 2, 0);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      logger.error("Error! Could not write analytics file!");
      e.printStackTrace();
    }
  }

}
