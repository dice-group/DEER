package org.aksw.deer.enrichment;

import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 */
public abstract class AbstractEnrichmentOperator implements EnrichmentOperator {

  protected List<Model> models;
  private ParameterMap parameterMap = null;
  private int inDegree = -1;
  private int outDegree = -1;

  public final void init(@NotNull ParameterMap parameterMap, int inDegree, int outDegree) {
    this.parameterMap = parameterMap;
    this.init(parameterMap);
    this.init(inDegree, outDegree);
  }

  public final void init(int inDegree, int outDegree) {
    if (!getDegreeBounds().satisfiedBy(inDegree, outDegree)) {
      //@todo: add better operatorinvalidarityexception
      throw new RuntimeException("Arity not valid!");
    } else {
      this.inDegree = inDegree;
      this.outDegree = outDegree;
    }
  }

  public final int getInDegree() {
    return inDegree;
  }

  public final int getOutDegree() {
    return outDegree;
  }

  @NotNull
  public final ParameterMap getParameterMap() {
    return parameterMap;
  }

  public final List<Model> apply(List<Model> models) {
    if (!isInitialized()) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    this.models = models;
    List<Model> result = process();
    // implicit cloning implemented here
    if (outDegree > result.size() && result.size() == 1) {
      for (int i = 0; i < outDegree - 1; i++) {
        Model clone = ModelFactory.createDefaultModel();
        clone.add(result.get(0));
        result.add(clone);
      }
    }
    return result;
  }

  public final Model apply(Model model) {
    List<Model> models = apply(List.of(model));
    if (models.isEmpty()) {
      return null;
    } else {
      return models.get(0);
    }
  }

  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,1,1,1);
  }

  public final boolean isInitialized() {
    return this.inDegree >= 0 && this.outDegree >= 0 && this.parameterMap != null;
  }

  protected abstract List<Model> process();

}
