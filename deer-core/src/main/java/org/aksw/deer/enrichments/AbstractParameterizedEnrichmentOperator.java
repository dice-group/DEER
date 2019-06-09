package org.aksw.deer.enrichments;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class AbstractParameterizedEnrichmentOperator extends AbstractParameterizedExecutionNode.WithImplicitCloning<Model> implements ParameterizedEnrichmentOperator {

  @NotNull
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


//  @Override
//  protected void writeInputAnalytics(List<Model> data) {
//    if (getInDegree() > 0) {
//      writeAnalytics("input sizes", data.stream().map(m -> String.valueOf(m.size())).reduce("( ", (a, b) -> a + b + " ") + ")");
//    }
//  }
//
//  @Override
//  protected void writeOutputAnalytics(List<Model> data) {
//    if (getOutDegree() > 0) {
//      writeAnalytics("output sizes", data.stream().map(m->String.valueOf(m.size())).reduce("( ", (a, b) -> a + b + " ") + ")");
//    }
//  }

}
