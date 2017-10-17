package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import java.util.List;
import org.aksw.deer.parameter.ParameterMap;
import org.aksw.deer.util.EnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public abstract class AbstractEnrichmentOperator implements EnrichmentOperator {

  protected static class DefaultDegreeBounds implements DegreeBounds {

    private final int minIn;
    private final int maxIn;
    private final int minOut;
    private final int maxOut;

    public DefaultDegreeBounds(int minIn, int maxIn, int minOut, int maxOut) {
      this.minIn = minIn;
      this.maxIn = maxIn;
      this.minOut = minOut;
      this.maxOut = maxOut;
    }

    @Override
    public int minIn() {
      return minIn;
    }

    @Override
    public int maxIn() {
      return maxIn;
    }

    @Override
    public int minOut() {
      return minOut;
    }

    @Override
    public int maxOut() {
      return maxOut;
    }

  }

  private ParameterMap parameterMap;
  protected List<Model> models;
  private int inArity;
  private int outArity;
  private boolean initialized = false;

  public void init(ParameterMap parameterMap, int inArity, int outArity) {
    if (!degreeInBounds(inArity, outArity)) {
      //@todo: add better operatorinvalidarityexception
      throw new RuntimeException("Arity not valid!");
    } else {
      this.accept(parameterMap);
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
    inBounds  = degreeBounds.minIn() <= in;
    inBounds &= degreeBounds.maxIn() >= in;
    inBounds &= degreeBounds.minOut() <= out;
    // we only need to check for maxOut if it is greater than 1 as for 1 we apply implicit cloning.
    if (degreeBounds.maxOut() > 1) {
      inBounds &= degreeBounds.maxOut() >= out;
    }
    return inBounds;
  }

  public Resource getType() {
    return DEER.resource(this.getClass().getCanonicalName());
  }

  public DegreeBounds getDegreeBounds() {
    return new DefaultDegreeBounds(1, 1, 1, 1);
  }

}
