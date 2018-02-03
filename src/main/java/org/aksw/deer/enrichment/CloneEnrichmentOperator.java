package org.aksw.deer.enrichment;

import java.util.List;
import org.aksw.deer.enrichment.ParameterlessEnrichmentOperator;
import org.aksw.deer.util.EnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.pf4j.Extension;

/**
 *  An {@code EnrichmentOperator} for copying models to multiple parallel processed outputs.
 *
 *  The {@code CloneEnrichmentOperator} is an {@link EnrichmentOperator} to enable parallel processing of different
 *  enrichments on the input model.
 *  To this end, it just copies the input models data to its n ≥ 2 outputs.
 *
 */
@Extension
public class CloneEnrichmentOperator extends ParameterlessEnrichmentOperator {

  @Override
  protected List<Model> process() {
    return models;
  }

}
