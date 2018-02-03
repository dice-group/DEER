package org.aksw.deer.enrichment;

import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;

/**
 */
public abstract class ParameterlessEnrichmentOperator extends AbstractEnrichmentOperator {

  @NotNull
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  @Override
  public void accept(@NotNull ParameterMap emptyParameterMap) { }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return ParameterMap.EMPTY_INSTANCE;
  }
}