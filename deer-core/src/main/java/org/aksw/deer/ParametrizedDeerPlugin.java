package org.aksw.deer;

import org.aksw.deer.DeerPlugin;
import org.aksw.faraday_cage.nodes.ParametrizedNode;
import org.apache.jena.rdf.model.Model;

public interface ParametrizedDeerPlugin extends DeerPlugin, ParametrizedNode<Model> {


}