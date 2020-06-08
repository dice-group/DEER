package org.aksw.deer.learning;

import org.aksw.faraday_cage.engine.ExecutionNode;

/**
 *
 */
public interface Learnable {

  ExecutionNode.DegreeBounds getLearnableDegreeBounds();

}
