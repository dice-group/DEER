/**
 * 
 */
package org.aksw.geolift.operators;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public interface ModelOperator {
	public List<Model> run(List<Model> models);
}
