package org.aksw.deer.decorators;

import org.aksw.deer.DeerExecutionGraphNode;
import org.aksw.deer.ParameterizedDeerExecutionGraphNode;
import org.aksw.faraday_cage.decorator.AbstractParameterizedExecutionGraphNodeDecorator;
import org.apache.jena.rdf.model.Model;

/**
 *
 */
public abstract class AbstractParameterizedDeerExecutionGraphNodeDecorator extends AbstractParameterizedExecutionGraphNodeDecorator<ParameterizedDeerExecutionGraphNode, Model> implements DeerExecutionGraphNode {

  public AbstractParameterizedDeerExecutionGraphNodeDecorator(ParameterizedDeerExecutionGraphNode other) {
    super(other);
  }

}