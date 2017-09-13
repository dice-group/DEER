package org.aksw.deer.enrichment;

import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public abstract class ParameterlessEnrichmentOperator extends AbstractEnrichmentOperator {

  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  @Override
  public void accept(ParameterMap emptyParameterMap) { }

  @Override
  public ParameterMap createParameterMap() {
    return ParameterMap.EMPTY_INSTANCE;
  }
}