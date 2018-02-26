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
    IdentifiableExecutionFactory<Model> deerFactory = new PluginFactory<>(DeerPlugin.class, pluginManager);
    ExecutionGraphBuilder<Model> executionGraphBuilder
      = new DefaultExecutionGraphBuilder<>(futureFactory);
    ExecutionGraphGenerator<Model> executionGraphGenerator
      = new ExecutionGraphGenerator<>(config, executionGraphBuilder, deerFactory);
    ExecutionGraph executionGraph = executionGraphGenerator.generate();
    time.split();
    logger.info("Execution model built after {}ms.", time.getSplitTime());
    logger.info("Starting execution of enrichment...");
    executionGraph.run();
    time.split();
    logger.info("Enrichment finished after {}ms", time.getSplitTime());
  }

}
