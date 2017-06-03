package org.aksw.deer.learning;

import java.util.List;
import org.aksw.deer.util.IEnrichmentFunction;
import org.aksw.deer.util.PluginFactory;

public interface PipelineLearner {

  List<IEnrichmentFunction> MODULES = new PluginFactory<>(IEnrichmentFunction.class).getImplementations();

}