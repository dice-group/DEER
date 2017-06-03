package org.aksw.deer.util;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.jena.rdf.model.Model;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface IEnrichmentFunction extends ExtensionPoint,
  BiFunction<Model, Map<String, String>, Model>, IPlugin {

  List<ParameterType> getParameterWithTypes();

  Map<String, String> selfConfig(Model source, Model target);

}
