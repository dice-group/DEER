package org.aksw.deer.decorators;

import org.aksw.deer.DeerExecutionGraphNode;
import org.aksw.faraday_cage.decorator.AbstractExecutionGraphNodeDecorator;
import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.apache.jena.rdf.model.Model;

/**
 *
 */
public abstract class AbstractDeerExecutionGraphNodeDecorator extends AbstractExecutionGraphNodeDecorator<Model> implements DeerExecutionGraphNode {

  public AbstractDeerExecutionGraphNodeDecorator(ExecutionGraphNode<Model> other) {
    super(other);
  }

}
