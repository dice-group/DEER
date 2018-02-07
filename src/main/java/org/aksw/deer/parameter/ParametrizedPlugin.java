package org.aksw.deer.parameter;

import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Base interface for parametrized plugins, defines a contract.
 * <p>
 * A {@code ParametrizedPlugin} is a class that is parametrized using a {@link ParameterMap}.
 * <br>
 * It should only be instantiated from instances of {@link ParametrizedPluginFactory}.
 * For this to work, each plugin type needs to define a <i>plugin type interface</i> that extends
 * at least {@code ParametrizedPlugin} and {@link org.pf4j.ExtensionPoint}.
 * Furthermore, each non-abstract class implementing a <i>plugin type interface</i> should be
 * annotated with {@link org.pf4j.Extension}.
 *
 */
public interface ParametrizedPlugin extends Consumer<ParameterMap> {

  /**
   * Get the type {@code ParametrizedPlugin}.
   * The type of a {@code ParametrizedPlugin} identifies the implementing class and <b>must</b> be
   * unique.
   * It is used to instantiate the class using the {@link ParametrizedPluginFactory}.
   * Defaults to using the implementing classes {@link Class#getSimpleName()}.
   *
   * @return  RDF resource identifying the implementation class
   */
  @NotNull
  default Resource getType() {
    return DEER.resource(this.getClass().getSimpleName());
  }

  /**
   * Create an uninitialized {@code ParameterMap} to be filled by the
   * {@link org.aksw.deer.execution.ExecutionModelGenerator}.
   *
   * @return  uninitialized {@code ParameterMap} containing all allowed {@code Parameter}
   */
  @NotNull
  ParameterMap createParameterMap();

  /**
   * Accept an initialized {@code ParameterMap} in order to configure this instance.
   *
   * @param params  an initialized {@code ParameterMap}
   */
  void accept(@NotNull ParameterMap params);

  /**
   * Get this instances configuration as an initialized {@code ParameterMap}.
   *
   * @return  an initialized {@code ParameterMap}.
   */
  @NotNull
  ParameterMap getParameterMap();

}
