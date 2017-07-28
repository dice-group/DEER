package org.aksw.deer.learning;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.util.IEnrichmentOperator;
import org.aksw.deer.util.PluginFactory;

public interface PipelineLearner {

  List<IEnrichmentOperator> MODULES = new ArrayList<>(new PluginFactory<>(AEnrichmentOperator.class).getImplementations());

}