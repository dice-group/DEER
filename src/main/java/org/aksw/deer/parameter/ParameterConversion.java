package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Kevin Dreßler
 */
public interface ParameterConversion {

  RDFNode serialize(Object object);

  Object deserialize(RDFNode node);

}