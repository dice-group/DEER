package org.aksw.deer;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.*;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Model;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 *
 */
public class Deer {

  private static final Logger logger = LoggerFactory.getLogger(Deer.class);

  public void run(Model config, PluginManager pluginManager, CompletableFutureFactory futureFactory) {
    Vocabulary.setDefaultURI(DEER.getURI());
    StopWatch time = new StopWatch();
    logger.info("Building execution model...");
    time.start();
    ExecutionFactory<Model> deerFactory = new PluginFactory<>(DeerPlugin.class, pluginManager);
    time.split();
    logger.trace("Factory initialized after {}ms.", time.getSplitTime());
    ExecutionGraph executionGraph = new ExecutionGraphGenerator(config).generate();
    time.split();
    logger.trace("Execution graph built after {}ms.", time.getSplitTime());
    executionGraph.compile(deerFactory, futureFactory);
    time.split();
    logger.info("Execution graph compiled after {}ms.", time.getSplitTime());
    logger.info("Starting execution of enrichment...");
    Analytics analytics = executionGraph.execute();
    time.split();
    logger.info("Enrichment finished after {}ms", time.getSplitTime());
    logger.info("Analytics information:\n------------------------------------------\n{}", analytics.toString());
  }

}
