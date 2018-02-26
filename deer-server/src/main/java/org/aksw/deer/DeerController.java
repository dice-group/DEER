package org.aksw.deer;

import org.aksw.deer.logging.MdcCompletableFuture;
import org.aksw.deer.server.Server;
import org.apache.commons.cli.*;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
    .addOption(Option.builder("s")
      .longOpt("server").desc("launch server").build())
    .addOption(Option.builder("p")
      .longOpt("port").desc("set port for server to listen on")
      .hasArg().argName("port_number").type(Number.class).build())
    ;

  private static final PluginManager pluginManager = new DefaultPluginManager();
  static {
    pluginManager.loadPlugins();
    pluginManager.startPlugins();
  }

  public static void main(String args[]) {
    CommandLine cl = parseCommandLine(args);
    if (cl.hasOption('h')) {
      printHelp();
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
      runDeer(cl.getArgList().get(0));
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

  public static void runDeer(String fileName) {
    logger.info("Trying to read DEER configuration from file {}...", fileName);
    try {
      Model model = ModelFactory.createDefaultModel();
      final long startTime = System.currentTimeMillis();
      model.read(fileName);
      logger.info("Loading {} is done in {}ms.", fileName, (System.currentTimeMillis() - startTime));
      runDeer(model);
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTPException trying to load model from " + fileName, e);
    }
  }

  private static void runDeer(Model configurationModel) {
    new Deer().run(configurationModel, pluginManager, MdcCompletableFuture.Factory.INSTANCE);
  }


}
