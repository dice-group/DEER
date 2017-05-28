package org.aksw.deer.plugin.operator;

import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

/**
 * @author sherif
 */
public interface IOperator {

  List<Model> process(List<Model> models, Map<String, String> parameters);

  List<String> getParameters();

  List<String> getNecessaryParameters();
}