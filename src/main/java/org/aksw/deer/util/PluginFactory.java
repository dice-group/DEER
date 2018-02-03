package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;

/**
 */

public class  PluginFactory <T extends ParametrizedPlugin> {

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
      throw new RuntimeException(clazz.getName() + " implementation for declaration \"" + id + "\" could not be found.");
    } else {
      Object o = factory.create(classMap.get(id));
      if (!clazz.isInstance(o)) {
        throw new RuntimeException("Plugin \"" + id + "\" required as " + clazz.getName() + ", but has type " + o.getClass().getName());
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

}