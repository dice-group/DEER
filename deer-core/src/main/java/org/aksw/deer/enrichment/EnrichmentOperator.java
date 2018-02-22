package org.aksw.deer.enrichment;

import org.aksw.deer.execution.graph.ExecutionNode;
import org.aksw.deer.parameter.ParameterMap;
import org.aksw.deer.parameter.ParametrizedPlugin;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;
import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * An Enrichment Operator.
 * <p>
 * An enrichment operator is an atomic operator on a list of RDF models, yielding a list of RDF models.
 * Its arity
 */
public interface EnrichmentOperator extends ExecutionNode, ParametrizedPlugin, ExtensionPoint {

  void init(@NotNull ParameterMap parameterMap, int inDegree, int outDegree);

  @NotNull
  ParameterMap selfConfig(Model source, Model target);

}