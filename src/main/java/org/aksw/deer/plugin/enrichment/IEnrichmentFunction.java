package org.aksw.deer.plugin.enrichment;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.aksw.deer.io.json.ParameterType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 */
public interface IEnrichmentFunction extends BiFunction<Model, Map<String, String>, Model> {

  List<String> getParameters();

  List<String> getNecessaryParameters();

  List<ParameterType> getParameterWithTypes();

  Map<String, String> selfConfig(Model source, Model target);

  Resource getType();
}
