package org.aksw.deer.util;

import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author Kevin Dreßler
 */
public interface EnrichmentOperator extends ExtensionPoint, UnaryOperator<Model>, Plugin {

  interface ArityBounds {
    int minIn();
    int maxIn();
    int minOut();
    int maxOut();
  }

  ArityBounds getArityBounds();

  int getInArity();
  int getOutArity();

  void init(ParameterMap parameterMap, int inArity, int outArity);

  ParameterMap selfConfig(Model source, Model target);

  ParameterMap getParameterMap();

  String getDescription();

  Resource getType();

  Model apply(Model model);

  List<Model> apply(List<Model> models);

  void accept(ParameterMap params);

}