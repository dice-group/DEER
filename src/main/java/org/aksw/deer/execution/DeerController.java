package org.aksw.deer.execution;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.aksw.deer.server.Server;
import org.apache.log4j.Logger;

public class DeerController {

  private static final Logger logger = Logger.getLogger(DeerController.class);
  private static final String HELP_MESSAGE =
    "To run DEER from command-line, provide the RDf configuration file as " +
      "the only one parameter for the DEER jar file. \n" +
      "Example: deer.jar config.ttl \n" +
      "For details about the configuration file see DEER manual at " +
      "https://github.com/GeoKnow/DEER/blob/master/DEER_manual/deer_manual.pdf ";

  public static void main(String args[]) throws IOException {
    if (args.length == 0 || args[0].equals("-?") || args[0].toLowerCase().equals("--help")) {
      logger.info(DeerController.HELP_MESSAGE);
    } else if (args[0].equals("-s") || args[0].toLowerCase().equals("--server")) {
      Server.main(args);
    } else if (args[0].equals("-l") || args[0].toLowerCase().equals("--list")) {
      org.aksw.deer.io.json.JSONConfigWriter.write();
    } else {
      // rdf config mode
      long startTime = System.currentTimeMillis();
      ExecutionModelGenerator executionModelGenerator = new ExecutionModelGenerator(args[0], new RunContext(0, ""));
      ExecutionModel executionModel = executionModelGenerator.generate();
      executionModel.execute();
      logger.info("DEER started execution");
      int i = 0;
      int interval = 10;
      TimeUnit unit = TimeUnit.MINUTES;
      while (!ForkJoinPool.commonPool().awaitQuiescence(interval, unit)) {
        i++;
        logger.info("DEER is alive (started execution " + unit.toMinutes(i*interval) + " minutes ago)");
      }
      Long totalTime = System.currentTimeMillis() - startTime;
      logger.info("Running DEER Done in " + totalTime + "ms");
    }
  }

}
