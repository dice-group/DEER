package org.aksw.deer.learning;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.util.EnrichmentOperator;
import org.aksw.deer.util.PluginFactory;

public interface PipelineLearner {

  List<EnrichmentOperator> MODULES = new ArrayList<>(new PluginFactory<>(AbstractEnrichmentOperator.class).getImplementations());

}