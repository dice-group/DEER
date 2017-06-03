package org.aksw.deer.util;

import java.util.List;

/**
 * @author Kevin Dreßler
 */
public interface IParameterized {

  List<String> getParameters();

  List<String> getNecessaryParameters();

}
