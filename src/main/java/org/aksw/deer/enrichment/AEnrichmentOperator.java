package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.aksw.deer.util.IEnrichmentOperator;
import org.aksw.deer.util.Parameter;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public abstract class AEnrichmentOperator implements IEnrichmentOperator {

  protected static class ArityBoundsImpl implements IEnrichmentOperator.ArityBounds{

    private final int minIn;
    private final int maxIn;
    private final int minOut;
    private final int maxOut;

    public ArityBoundsImpl(int minIn, int maxIn, int minOut, int maxOut) {
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

  protected Map<String, String> parameters;
  protected List<Model> models;
  private int inArity;
  private int outArity;
  private boolean initialized = false;

  public void init(Map<String, String> parameters, int inArity, int outArity) {
    if (arityInBounds(inArity, outArity)) {
      applyParameters(parameters);
      this.inArity = inArity;
      this.outArity = outArity;
      this.initialized = true;
    } else {
      //@todo: add better operatorinvalidarityexception
      throw new RuntimeException("Arity not valid!");
    }
  }

  private void applyParameters(Map<String, String> providedParameters) {
    List<Parameter> parameters = this.getParameters();
    for (Parameter p : parameters) {
      if (p.isRequired() && providedParameters.get(p.getName()) != null) {
        throw new RuntimeException("Required parameter " + p.getName() + " not defined for " + this.getType());
      } else if (providedParameters.get(p.getName()) != null) {
        p.getAssignmentConsumer().accept(providedParameters.get(p.getName()));
      } else {
        p.getAssignmentConsumer().accept(p.getDefaultValue());
      }
    }
  }

  public void init(Map<String, String> parameters) {
    init(parameters, 1, 1);
  }

  public int getInArity() {
    return inArity;
  }

  public int getOutArity() {
    return outArity;
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

  public List<Model> apply(Model model) {
    return apply(Lists.newArrayList(model));
  }

  protected abstract List<Model> process();

  private boolean arityInBounds(int in, int out) {
    boolean inBounds;
    ArityBounds arityBounds = getArityBounds();
    inBounds  = arityBounds.minIn() <= in;
    inBounds &= arityBounds.maxIn() >= in;
    inBounds &= arityBounds.minOut() <= out;
    // we only need to check for maxOut if it is greater than 1 as for 1 we apply implicit cloning.
    if (arityBounds.maxOut() > 1) {
      inBounds &= arityBounds.maxOut() >= out;
    }
    return inBounds;
  }

  public Resource getType() {
    return DEER.resource(this.getClass().getCanonicalName());
  }

  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1, 1, 1, 1);
  }

}
