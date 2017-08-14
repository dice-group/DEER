package org.aksw.deer.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author sherif
 */
public interface IEnrichmentOperator extends ExtensionPoint, UnaryOperator<Model>, IPlugin {

  interface ArityBounds {
    int minIn();
    int maxIn();
    int minOut();
    int maxOut();
  }

  ArityBounds getArityBounds();

  int getInArity();
  int getOutArity();

  void init(Map<String, String> parameters, int inArity, int outArity);

  Map<String, String> selfConfig(Model source, Model target);

  List<Parameter> getParameters();

  String getDescription();

  Resource getType();

  Model apply(Model model);

  List<Model> apply(List<Model> models);

}