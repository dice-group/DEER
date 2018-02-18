package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Resource;

import java.util.Set;

/**
 * A {@code ParameterMap} serves as registry of {@code Parameter}s that can be used
 * to configure a {@code ParametrizedPlugin}.
 * <p>
 * A {@code ParameterMap} holds a list of {@link Parameter} instances upon its
 * initialization, therefore acting as a registry of allowed {@code Parameter}s.
 * <br>
 * Furthermore, a {@code ParameterMap} supports setting values of its parameters
 * either {@link #setValue(Parameter, Object) directly} or {@link #init(Resource) automatically}
 * via {@link ParameterConversion} of a supplied RDF subgraph.
 * <br>
 * Calling {@link #init(Resource)} on a given {@code ParameterMap} will change its state to
 * initialized and prevent further modification of its values, effectively making it immutable.
 * It is mandatory to call {@link #init(Resource)} before getting values from a {@code ParameterMap}.
 */
public interface ParameterMap {

  /**
   * An empty, unmodifiable instance
   */
  ParameterMap EMPTY_INSTANCE = new ParameterMapImpl().init(null);

  /**
   * @return a list of this {@code ParameterMap}s supported parameters
   */
  Set<Parameter> getAllParameters();

  /**
   * Directly set a value for a parameter.
   *
   * @param p  the parameter which should be set
   * @param node  the value to set for the given parameter
   * @return  this {@code ParameterMap}, for method chaining
   * @throws IllegalStateException  if this {@code ParameterMap} has already been initialized
   */
  ParameterMap setValue(Parameter p, Object node) throws IllegalStateException;

  /**
   * Get the value for the given parameter.
   * Makes use of return type inference to avoid manual casting.
   *
   * @param p  the parameter to get
   * @param <T>  this methods generic return type
   * @return  the value for the given parameter
   * @throws IllegalStateException  if this {@code ParameterMap} has not been initialized yet
   */
  <T> T getValue(Parameter p) throws IllegalStateException;

  /**
   * Get the value for the given parameter, or the defaultValue, if it has not been set.
   * Makes use of return type inference to avoid manual casting.
   *
   * @param p  the parameter to get
   * @param defaultValue  a defaultValue as fallback
   * @param <T>   this methods generic return type
   * @return  {@code defaultVaue} if the value has not been set or has been set to {@code null};
   *          otherwise, the value of parameter {@code p}
   * @throws IllegalStateException  if this {@code ParameterMap} has not been initialized yet
   */
  <T> T getValue(Parameter p, T defaultValue) throws IllegalStateException;

  /**
   * Initialize this {@code ParameterMap}, making it immutable and enabling the getters.
   *
   * @param r  if not null, this {@code Resource} and its corresponding subgraph will be used for
   *           automatic parameter value assignment
   * @return  this {@code ParameterMap}, for method chaining
   */
  ParameterMap init(Resource r);

}