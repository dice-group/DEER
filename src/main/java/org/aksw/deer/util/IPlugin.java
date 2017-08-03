package org.aksw.deer.util;

import java.util.List;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public interface IPlugin {

  List<Parameter> getParameters();

  String getDescription();

  Resource getType();

}
