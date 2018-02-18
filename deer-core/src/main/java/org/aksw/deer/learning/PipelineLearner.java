package org.aksw.deer.learning;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.enrichment.EnrichmentOperator;
import org.aksw.deer.parameter.ParametrizedPluginFactory;

public interface PipelineLearner {

  List<EnrichmentOperator> MODULES = new ArrayList<>(new ParametrizedPluginFactory<>(AbstractEnrichmentOperator.class).getImplementations());

}