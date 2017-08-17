package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public class JenaBackedParameterMap extends BaseParameterMap {

  public void init(Resource parameterRoot) {
    for (Parameter parameter : parameters) {
      if (parameterRoot.hasProperty(parameter.getProperty())) {
        setValue(parameter, parameterRoot.getProperty(parameter.getProperty()).getObject().toString());
      }
    }
    this.init();
  }

}