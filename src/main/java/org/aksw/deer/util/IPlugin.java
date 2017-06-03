package org.aksw.deer.util;

import java.util.List;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public interface IPlugin extends IParameterized {

  List<String> getParameters();

  List<String> getNecessaryParameters();

  String id();

  String getDescription();

  Resource getType();

}
