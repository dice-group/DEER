package org.aksw.deer.enrichment.predicateconformation;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.aksw.deer.parameter.ParameterConversion;
import org.aksw.deer.parameter.StringParameterConversion;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kevin Dreßler
 */
@SuppressWarnings("unchecked")
public class PropertyMapParameterConversion implements ParameterConversion {

  static final PropertyMapParameterConversion INSTANCE = new PropertyMapParameterConversion();
  private PropertyMapParameterConversion() { }

  @Override
  public RDFNode serialize(Object object) {
    Map<Property,Property> propertyMap = (Map<Property,Property>) object;

    return null;
  }

  @Override
  public Object deserialize(RDFNode node) {
    Map<Property,Property> propertyMap = new HashMap<>();
    return propertyMap;
  }
}
