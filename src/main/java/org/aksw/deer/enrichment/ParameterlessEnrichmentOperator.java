package org.aksw.deer.enrichment;

import org.aksw.deer.parameter.EmptyParameterMap;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public abstract class ParameterlessEnrichmentOperator extends AbstractEnrichmentOperator<EmptyParameterMap> {

  @Override
  public EmptyParameterMap selfConfig(Model source, Model target) {
    return EmptyParameterMap.INSTANCE;
  }

  @Override
  public void accept(EmptyParameterMap emptyParameterMap) {

  }

  @Override
  public EmptyParameterMap createParameterMap() {
    return EmptyParameterMap.INSTANCE;
  }
}