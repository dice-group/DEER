package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;

/**
 * A {@code ParameterConversion} to bridge between RDF
 * and native Java class representation of data.
 * <p>
 * A ParameterConversion is implicitly tied to a Java type which it expects
 * objects passed to {@link #toRDF(Object)} to be instances of and which
 * objects returned from {@link #fromRDF(RDFNode)} are instances of.
 */
public interface ParameterConversion {

  /**
   * Convert the given {@code object} to an equal RDF representation.
   * @param object an object expected to be instance of the type that this
   *               {@code ParameterConversion} is intended for
   * @return the RDF representation of the given {@code object}
   */
  RDFNode toRDF(Object object);

  /**
   * Convert the given {@code node} to an equal object representation.
   * @param node the RDF data to be converted to an instance of the type that
   *             this {@code ParameterConversion} is intended for
   * @return the object representation of the given {@code node}
   */
  Object fromRDF(RDFNode node);

}