package org.aksw.deer.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.aksw.deer.util.IParameterized;
import org.aksw.deer.util.IPlugin;
import org.apache.jena.rdf.model.Model;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface IOperator extends ExtensionPoint, IPlugin, UnaryOperator<List<Model>> {

  interface OperatorArity {
    int getInArity();
    int getOutArity();
  }

  OperatorArity getArity();

  void init(Map<String, String> parameters, int inArity, int outArity);

}