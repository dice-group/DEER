package org.aksw.deer.enrichments;

import org.aksw.faraday_cage.nodes.ParametrizedNode;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;

/**
 * An Enrichment Operator.
 * <p>
 * An enrichments operator is an atomic operator on a list of RDF models, yielding a list of RDF models.
 * Its arity
 */
public interface ParametrizedEnrichmentOperator extends EnrichmentOperator, ParametrizedNode<Model> {

  @NotNull
  ParameterMap selfConfig(Model source, Model target);

}