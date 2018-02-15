package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 */
public abstract class AbstractEnrichmentOperator implements EnrichmentOperator {

  private ParameterMap parameterMap;
  protected List<Model> models;
  private int inArity;
  private int outArity;
  private boolean initialized = false;

  public void init(@NotNull ParameterMap parameterMap, int inArity, int outArity) {
    if (!degreeInBounds(inArity, outArity)) {
      //@todo: add better operatorinvalidarityexception
      throw new RuntimeException("Arity not valid!");
    } else {
      this.init(parameterMap);
      this.parameterMap = parameterMap;
      this.inArity = inArity;
      this.outArity = outArity;
      this.initialized = true;
    }
  }

  public int getInDegree() {
    return inArity;
  }

  public int getOutDegree() {
    return outArity;
  }

  @NotNull
  public ParameterMap getParameterMap() {
    return parameterMap;
  }

  public List<Model> apply(List<Model> models) {
    if (!this.initialized) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    this.models = models;
    List<Model> result = process();
    // implicit cloning implemented here
    if (outArity > result.size() && result.size() == 1) {
      for (int i = 0; i < outArity - 1; i++) {
        Model clone = ModelFactory.createDefaultModel();
        clone.add(result.get(0));
        result.add(clone);
      }
    }
    return result;
  }

  public Model apply(Model model) {
    List<Model> models = apply(Lists.newArrayList(model));
    if (models.isEmpty()) {
      return null;
    } else {
      return models.get(0);
    }
  }

  protected abstract List<Model> process();

  private boolean degreeInBounds(int in, int out) {
    boolean inBounds;
    DegreeBounds degreeBounds = getDegreeBounds();
    inBounds  = in >= degreeBounds.minIn();
    inBounds &= in <= degreeBounds.maxIn();
    inBounds &= out >= degreeBounds.minOut();
    // we only need to check for maxOut if it is greater than 1 as for 1 we apply implicit cloning.
    if (degreeBounds.maxOut() > 1) {
      inBounds &= out <= degreeBounds.maxOut();
    }
    return inBounds;
  }

  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1, 1, 1, 1);
  }

}
