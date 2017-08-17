package org.aksw.deer.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.aksw.deer.parameter.JenaResourceConsumingParameter;
import org.aksw.deer.parameter.Parameter;
import org.aksw.deer.parameter.ParameterAssigner;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface EnrichmentOperator<T extends ParameterMap> extends ExtensionPoint, UnaryOperator<Model>, Plugin<T> {

  interface ArityBounds {
    int minIn();
    int maxIn();
    int minOut();
    int maxOut();
  }

  ArityBounds getArityBounds();

  int getInArity();
  int getOutArity();

  void init(T parameterMap, int inArity, int outArity);

  T selfConfig(Model source, Model target);

  T getParameterMap();

  T createParameterMap();

  String getDescription();

  Resource getType();

  Model apply(Model model);

  List<Model> apply(List<Model> models);

  void accept(T t);

}