package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A singleton no-op {@code ParameterConversion}
 */
public class RDFNodePassthrough implements ParameterConversion {

  /**
   * single instance
   */
  private static final RDFNodePassthrough INSTANCE = new RDFNodePassthrough();

  /**
   * Get single instance
   * @return single {@code RDFNodePassthrough} instance
   */
  public static RDFNodePassthrough getInstance() {
    return INSTANCE;
  }

  /**
   * private constructor
   */
  private RDFNodePassthrough() {}

  @Override
  @SuppressWarnings("unchecked")
  public RDFNode toRDF(Object object) {
    return (RDFNode) object;
  }

  @Override
  public Object fromRDF(RDFNode node) {
    return node;
  }
}
