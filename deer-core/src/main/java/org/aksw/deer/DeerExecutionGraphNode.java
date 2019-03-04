package org.aksw.deer;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.apache.jena.rdf.model.Model;

/**
 * Reference type for all DEER implementations of {@link ExecutionGraphNode} needed to specify the scope of
 * the faraday cage plugin system.
 */
public interface DeerExecutionGraphNode extends ExecutionGraphNode<Model> {

}