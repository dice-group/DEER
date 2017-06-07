package org.aksw.deer.operator;

import java.util.List;
import java.util.Map;
import org.aksw.deer.util.IOperator;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public abstract class AOperator implements IOperator {

  protected OperatorArity arity;
  protected Map<String, String> parameters;
  protected List<Model> models;

  protected static class OperatorArityImpl implements IOperator.OperatorArity{
    private int in, out;
    OperatorArityImpl(int in, int out) {
      this.in = in;
      this.out = out;
    }

    @Override
    public int getInArity() {
      return in;
    }

    @Override
    public int getOutArity() {
      return out;
    }
  }

  public void init(Map<String, String> parameters, int inArity, int outArity) {
    this.parameters = parameters;
    this.arity = new OperatorArityImpl(inArity, outArity);
  }

  public List<Model> apply(List<Model> modelCollection) {
    this.models = modelCollection;
    if (this.parameters == null) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    return process();
  }

  protected abstract List<Model> process();

  public OperatorArity getArity() {
    return arity;
  }

}
