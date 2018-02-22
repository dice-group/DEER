package org.aksw.deer.server;

import org.aksw.deer.execution.ExecutionModel;
import org.aksw.deer.execution.ExecutionModelGenerator;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.util.CompletableFutureFactory;
import org.aksw.deer.logging.MdcCompletableFuture;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Model;
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
    MDC.put("requestId","../log");
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
    CompletableFutureFactory.setImplementation(MdcCompletableFuture.Factory.INSTANCE);
    Server.run(port);
  }

  static void runDeer(String fileName) {
    logger.info("Trying to read DEER configuration from file {}...", fileName);
    runDeer(new ModelReader().readModel(fileName));
  }

  private static void runDeer(Model configurationModel) {
    StopWatch time = new StopWatch();
    logger.info("Building execution model...");
    time.start();
    ExecutionModelGenerator executionModelGenerator = new ExecutionModelGenerator(configurationModel);
    ExecutionModel executionModel = executionModelGenerator.generate();
    time.split();
    logger.info("Execution model built after {}ms.", time.getSplitTime());
    logger.info("Starting enrichment execution...");
    executionModel.execute();
    time.split();
    logger.info("Enrichment finished after {}ms", time.getSplitTime());
  }

}
