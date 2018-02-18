package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A singleton {@code ParameterConversion} that converts between {@code RDFNode} and {@code String}.
 */
public class StringParameterConversion implements ParameterConversion {
  /**
   * Single instance
   */
  private static final StringParameterConversion INSTANCE = new StringParameterConversion();

  /**
   * Get single instance
   * @return single {@code StringParameterConversion} instance
   */
  public static StringParameterConversion getInstance() {
    return INSTANCE;
  }

  /**
   * private constructor
   */
  private StringParameterConversion() { }

  @Override
  public RDFNode toRDF(Object object) {
    Model m = ModelFactory.createDefaultModel();
    return m.createLiteral(object.toString());
  }

  @Override
  public Object fromRDF(RDFNode node) {
    return node.toString();
  }

}