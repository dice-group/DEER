package org.aksw.deer.enrichments;

import org.aksw.deer.DeerPlugin;
import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.nodes.AbstractNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 */
public abstract class AbstractEnrichmentOperator extends AbstractNode.WithImplicitCloning<Model> implements DeerPlugin {

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

  @Override
  protected void writeInputAnalytics(List<Model> data) {

  }

  @Override
  protected void writeOutputAnalytics(List<Model> data) {

  }
}
