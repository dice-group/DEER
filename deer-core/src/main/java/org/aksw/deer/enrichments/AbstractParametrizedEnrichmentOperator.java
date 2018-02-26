package org.aksw.deer.enrichments;

import org.aksw.deer.ParametrizedDeerPlugin;
import org.aksw.deer.learning.SelfConfigurator;
import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.nodes.AbstractParametrizedNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class AbstractParametrizedEnrichmentOperator extends AbstractParametrizedNode.WithImplicitCloning<Model> implements ParametrizedDeerPlugin, SelfConfigurator {

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
    return Vocabulary.resource(this.getClass().getSimpleName());
  }
}
