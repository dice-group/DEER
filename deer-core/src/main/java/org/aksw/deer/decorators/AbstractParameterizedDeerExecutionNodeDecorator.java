package org.aksw.deer.decorators;

import org.aksw.deer.DeerExecutionNode;
import org.aksw.deer.ParameterizedDeerExecutionNode;
import org.aksw.faraday_cage.decorator.AbstractParameterizedExecutionNodeDecorator;
import org.apache.jena.rdf.model.Model;

/**
 *
 */
public abstract class AbstractParameterizedDeerExecutionNodeDecorator extends AbstractParameterizedExecutionNodeDecorator<ParameterizedDeerExecutionNode, Model> implements DeerExecutionNode {

  public AbstractParameterizedDeerExecutionNodeDecorator(ParameterizedDeerExecutionNode other) {
    super(other);
  }

}