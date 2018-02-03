package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;

/**
 */
public class RDFNodePassthrough implements ParameterConversion {

  private static final RDFNodePassthrough INSTANCE = new RDFNodePassthrough();

  public static RDFNodePassthrough getInstance() {
    return INSTANCE;
  }

  private RDFNodePassthrough() {}

  @Override
  @SuppressWarnings("unchecked")
  public RDFNode serialize(Object object) {
    return (RDFNode) object;
  }

  @Override
  public Object deserialize(RDFNode node) {
    return node;
  }
}
