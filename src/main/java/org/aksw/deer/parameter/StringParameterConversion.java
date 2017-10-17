package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Kevin Dreßler
 */
public class StringParameterConversion implements ParameterConversion {

  private static final StringParameterConversion INSTANCE = new StringParameterConversion();

  public static StringParameterConversion getInstance() {
    return INSTANCE;
  }

  private StringParameterConversion() { }

  @Override
  public RDFNode serialize(Object object) {
    Model m = ModelFactory.createDefaultModel();
    return m.createLiteral(object.toString());
  }

  @Override
  public Object deserialize(RDFNode node) {
    return node.toString();
  }

}