package org.aksw.deer.util;

import java.util.List;
import java.util.Map;
import org.aksw.deer.util.IParameterized;
import org.aksw.deer.util.IPlugin;
import org.apache.jena.rdf.model.Model;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface IOperator extends ExtensionPoint, IPlugin {

  List<Model> process(List<Model> models, Map<String, String> parameters);

}