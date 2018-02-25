package org.aksw.deer.enrichments;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.nodes.AbstractNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class AbstractEnrichmentOperator extends AbstractNode.WithImplicitCloning<Model> implements EnrichmentOperator {

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,1,1,1);
  }

  protected final Model deepCopy(Model model) {
    return ModelFactory.createDefaultModel().add(model);
  }

  @NotNull
  @Override
  public Resource getType() {
    return DEER.resource(this.getClass().getSimpleName());
  }

}
