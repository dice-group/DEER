package org.aksw.deer.decorators;

import org.aksw.deer.DeerExecutionGraphNode;
import org.aksw.deer.DeerExecutionNodeWrapper;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.decorator.AbstractExecutionGraphNodeWrapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class AbstractDeerExecutionGraphNodeWrapper extends AbstractExecutionGraphNodeWrapper<DeerExecutionGraphNode, Model> implements DeerExecutionNodeWrapper {

  @Override
  public @NotNull Resource getType() {
    return DEER.resource(this.getClass().getSimpleName());
  }
}
