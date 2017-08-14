package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.PluginManager;

/**
 * @author sherif
 */

public class PluginFactory <T extends IPlugin> {

  private static final PluginManager pluginManager = new DefaultPluginManager();
  private ExtensionFactory factory;
  private Map<String, Class<?>> classMap;
  private Class<T> clazz;

  public PluginFactory(Class<T> clazz) {
    this.clazz = clazz;
    this.factory = pluginManager.getExtensionFactory();
    this.classMap = createClassMap();
  }

  private Map<String, Class<?>> createClassMap() {
    Map<String, Class<?>> classMap = new HashMap<>();
    pluginManager.getExtensions(clazz).forEach(
      (aef) -> classMap.put(aef.getType().toString(), aef.getClass())
    );
    return classMap;
  }

  public T create(String id) {
    if (!classMap.containsKey(id)) {
      throw new RuntimeException(clazz.getCanonicalName() + " implementation for declaration \"" + id + "\" could not be found.");
    } else {
      Object o = factory.create(classMap.get(id));
      if (!clazz.isInstance(o)) {
        throw new RuntimeException("Plugin \"" + id + "\" required as " + clazz.getCanonicalName() + ", but has type " + o.getClass().getCanonicalName());
      } else {
        return (T) o;
      }
    }
  }

  /**
   * @return list of names of all implemented enrichment functions
   */
  public List<String> getNames() {
    return new ArrayList<>(classMap.keySet());
  }

  /**
   * @return list of instances of all implemented enrichment functions
   */
  public List<T> getImplementations() {
    return classMap.values().stream()
      .map(c -> (T) factory.create(c))
      .collect(Collectors.toList());
  }

  public String getDescription(String id) {
    if (!classMap.containsKey(id)) {
      throw new RuntimeException("Implementation for declaration \"" + id + "\" could not be found.");
    } else {
      return ((IEnrichmentOperator) factory.create(classMap.get(id))).getDescription();
    }
  }
//  public static final String DEREFERENCING_MODULE_DESCRIPTION =
//    "The purpose of the org.aksw.deer.resources.dereferencing enrichment is to extend the model's Geo-spatial" +
//      "information by set of information through specified predicates";
//  public static final String LINKING_MODULE_DESCRIPTION =
//    "The purpose of the org.aksw.deer.resources.linking enrichment is to enrich a model with additional " +
//      "geographic information URIs represented in owl:sameAs predicates";
//  public static final String NLP_MODULE_DESCRIPTION =
//    "The purpose of the NLP enrichment is to enrich a model with additional Geo-" +
//      "spatial information URIs represented by the addedGeoProperty predicates, " +
//      "witch by default is geoknow:relatedTo predicates";
//  public static final String AUTHORITY_CONFORMATION_MODULE_DESCRIPTION =

//  public static final String PREDICATE_CONFORMATION_MODULE_DESCRIPTION =
//    "The purpose of the predicate conformation enrichment is to change a set of source predicates to a set of target predicates."
//      +
//      "For example, all rdfs:label can be conformed to become skos:prefLabel";
//  public static final String FILTER_MODULE_DESCRIPTION =
//    "Runs a set of triples patterns against an input model to filter some triples out " +
//      "of it and export them to an output model. For example running triple pattern " +
//      "'?s <http://dbpedia.org/ontology/abstract> ?o' againt an input model containing " +
//      "'http://dbpedia.org/resource/Berlin' will generate output model containing only " +
//      "Berlin's abstracts of DBpedia";
}