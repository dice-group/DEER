/**
 *
 */
package org.aksw.deer.modules;

import java.util.List;
import java.util.Map;
import org.aksw.deer.json.ParameterType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 */
public interface DeerModule {

  Model process(Model model, Map<String, String> parameters);

  List<String> getParameters();

  List<String> getNecessaryParameters();

  Map<String, String> selfConfig(Model source, Model target);

  List<ParameterType> getParameterWithTypes();

  Resource getType();
}
