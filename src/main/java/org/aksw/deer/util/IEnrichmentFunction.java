package org.aksw.deer.util;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.apache.jena.rdf.model.Model;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface IEnrichmentFunction extends UnaryOperator<Model>, IPlugin, ExtensionPoint {

  List<ParameterType> getParameterWithTypes();

  Map<String, String> selfConfig(Model source, Model target);

  void init(Map<String, String> parameters);

}
