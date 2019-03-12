package org.aksw.deer;

import org.aksw.faraday_cage.engine.FaradayCageContext;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 *
 */
public final class Deer {

  private static final Logger logger = LoggerFactory.getLogger(Deer.class);

  public static synchronized FaradayCageContext getExecutionContext(PluginManager pluginManager) {
    return FaradayCageContext.of(
      DeerExecutionNode.class,
      DeerExecutionNodeWrapper.class,
      pluginManager);
  }

}
