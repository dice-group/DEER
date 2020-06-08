package org.aksw.deer;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.apache.jena.rdf.model.Model;

/**
 * Reference type for all DEER implementations of {@link ExecutionNode} needed to specify the scope of
 * the faraday cage plugin system.
 */
public interface DeerExecutionNode extends ExecutionNode<Model> {

}