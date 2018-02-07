package org.aksw.deer.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.deer.parameter.ParametrizedPlugin;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;

/**
 * A generic factory for implementations of {@code ParametrizedPlugin}.
 * <p>
 * Create an instance of this
 */
public class ParametrizedPluginFactory<T extends ParametrizedPlugin> {

  /**
   * pf4j plugin manager
   */
  private final PluginManager pluginManager = new DefaultPluginManager();
  /**
   * pf4j extension factory
   */
  private ExtensionFactory factory;
  /**
   * map of class names to class instances
   */
  private Map<String, Class<?>> classMap;
  /**
   * this {@code ParametrizedPluginFactory}s type parameters {@code Class} instance
   */
  private Class<T> clazz;

  /**
   * Constructor, takes an instance of this {@code ParametrizedPluginFactory}s type parameters
   * {@code Class}.
   *
   * @param clazz {@code Class} instance for this {@code ParametrizedPluginFactory}s type parameter
   */
  public ParametrizedPluginFactory(Class<T> clazz) {
    this.clazz = clazz;
    this.factory = pluginManager.getExtensionFactory();
    this.classMap = createClassMap();
  }

  /**
   * Generates the class map for instances of the interface referenced by this
   * {@code ParametrizedPluginFactory}s type parameter.
   *
   * @return the class map for this {@code ParametrizedPluginFactory}s type parameter
   */
  private Map<String, Class<?>> createClassMap() {
    Map<String, Class<?>> classMap = new HashMap<>();
    pluginManager.getExtensions(clazz).forEach(
      (aef) -> classMap.put(aef.getType().toString(), aef.getClass())
    );
    return classMap;
  }

  /**
   * Create an instance of the {@code ParametrizedPlugin} identified by {@code id}.
   *
   * @param id  identifier of the class to instantiate
   * @return  instance of the {@code ParametrizedPlugin} identified by {@code id}.
   */
  public T create(String id) {
    if (!classMap.containsKey(id)) {
      throw new RuntimeException(clazz.getName() + " implementation for declaration \"" + id
        + "\" could not be found.");
    } else {
      Object o = factory.create(classMap.get(id));
      if (!clazz.isInstance(o)) {
        throw new RuntimeException("Plugin \"" + id + "\" required as " + clazz.getName()
          + ", but has type " + o.getClass().getName());
      } else {
        return (T) o;
      }
    }
  }

  /**
   * List the names of all implementations of {@code T}
   *
   * @return list of names of all implementations of {@code T}
   */
  public List<String> getNames() {
    return new ArrayList<>(classMap.keySet());
  }

  /**
   * List instances of all implementations of {@code T}
   *
   * @return list of instances of all implementations of {@code T}
   */
  public List<T> getImplementations() {
    return classMap.values().stream()
      .map(c -> (T) factory.create(c))
      .collect(Collectors.toList());
  }

}