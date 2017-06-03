package org.aksw.deer.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * @author Kevin Dreßler
 */
public class Parameters {

  private ImmutableMap<String, String> params;

  /**
   * Constructor accepting a map for immutable encapsulation.
   * @param params parameter map
   */
  public Parameters(Map<String, String> params) {
    this.params = new Builder<String, String>().putAll(params).build();
  }

  /**
   * Constructor for resources, will automatically generate parameter map.
   * @param moduleOrOperator resource representing a deer enrichment (aef or operator)
   */
  public Parameters(Resource moduleOrOperator) {
    this(getParameters(moduleOrOperator));
  }

  /**
   * Getter proxy for member map.
   * @param key
   * @return
   */
  public String get(String key) {
    return params.get(key);
  }

  /**
   * Return an stream of parameters as instances of Map.Entry<String, String>
   * @return Stream of entries of the member map.
   */
  public Stream<Entry<String, String>> stream() {
    return params.entrySet().stream();
  }

  /**
   * Read parameters from Jena Resource
   * @param moduleOrOperator
   * Resource from which to build parameters.
   * Needs to be linked to a valid model.
   * @return Parameter map
   */
  private static ImmutableMap<String, String> getParameters(Resource moduleOrOperator) {
    Builder<String, String> mapBuilder = new Builder<>();
    StmtIterator it = moduleOrOperator.listProperties(SPECS.hasParameter);
    while (it.hasNext()) {
      Resource parameter = it.next().getObject().asResource();
      if (!parameter.hasProperty(SPECS.hasKey) || !parameter.hasProperty(SPECS.hasValue)){
        continue;
      }
      String key = parameter.getProperty(SPECS.hasKey).getObject().toString();
      String value = parameter.getProperty(SPECS.hasValue).getObject().toString();
      mapBuilder.put(key, value);
    }
    return mapBuilder.build();
  }

}
