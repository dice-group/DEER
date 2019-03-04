package org.aksw.deer.enrichments;

import org.aksw.deer.ParameterizedDeerExecutionGraphNode;
import org.apache.jena.rdf.model.Model;
import org.pf4j.Extension;

import java.util.List;

/**
 *  An {@code EnrichmentOperator} for copying models to multiple parallel processed outputs.
 *
 *  The {@code CloneEnrichmentOperator} is an {@link ParameterizedDeerExecutionGraphNode} to enable parallel processing of different
 *  enrichments on the input model.
 *  To this end, it just copies the input models data to its n ≥ 2 outputs.
 *
 */
@Extension
public class CloneEnrichmentOperator extends AbstractEnrichmentOperator {

  @Override
  protected List<Model> safeApply(List<Model> models) {
    return models;
  }

}
