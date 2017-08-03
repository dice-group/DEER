package org.aksw.deer.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * @author Kevin Dreßler
 */
public class ParameterReader {

  /**
   * Read parameters from Jena Resource
   * @param moduleOrOperator
   * Resource from which to build parameters.
   * Needs to be linked to a valid model.
   * @return Parameter map
   */
  public static ImmutableMap<String, String> getParameters(Resource moduleOrOperator) {
    Builder<String, String> mapBuilder = new Builder<>();
    StmtIterator it = moduleOrOperator.listProperties(DEER.hasParameter);
    while (it.hasNext()) {
      Resource parameter = it.next().getObject().asResource();
      if (!parameter.hasProperty(DEER.hasKey) || !parameter.hasProperty(DEER.hasValue)){
        continue;
      }
      String key = parameter.getProperty(DEER.hasKey).getObject().toString();
      String value = parameter.getProperty(DEER.hasValue).getObject().toString();
      mapBuilder.put(key, value);
    }
    return mapBuilder.build();
  }

}