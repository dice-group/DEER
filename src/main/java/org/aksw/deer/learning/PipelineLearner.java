/**
 *
 */
package org.aksw.deer.learning;

import java.util.List;
import org.aksw.deer.plugin.enrichment.IEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.EnrichmentFunctionFactory;

/**
 * @author sherif
 */
public interface PipelineLearner {

  List<IEnrichmentFunction> MODULES = EnrichmentFunctionFactory.getImplementations();
}