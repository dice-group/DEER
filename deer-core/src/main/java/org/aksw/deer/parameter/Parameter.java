package org.aksw.deer.parameter;

import org.aksw.deer.parameter.conversion.ParameterConversion;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A {@code Parameter} for configuring classes.
 * <p>
 * A {@code Parameter} is made up of a {@link Property}, a
 * {@link ParameterConversion} and either required or optional.
 * <p>
 * Implementations of {@link Object#hashCode()} and {@link Object#equals(Object)}
 * must mirror the respective behaviour of the encapsulated {@code Property},
 * that is, a {@code Parameter}'s identity is agnostic of its
 * {@code ParameterConversion} and whether it is required or not.
 *
 */
public interface Parameter {

  /**
   * @return true if this {@code Parameter} is required
   */
  boolean isRequired();

  /**
   * @return the {@code Property} of this {@code Parameter}
   */
  Property getProperty();

  /**
   * Use this {@code Parameter}'s {@code ParameterConversion} to
   * toRDF the given {@code object}.
   * @param object the object getting serialized
   * @return serialization of {@code object}
   */
  RDFNode applySerialization(Object object);

  /**
   * Use this {@code Parameter}'s {@code ParameterConversion} to
   * fromRDF the given {@code node}.
   * @param node the node getting deserialized
   * @return deserialization of {@code node}
   */
  Object applyDeserialization(RDFNode node);

}