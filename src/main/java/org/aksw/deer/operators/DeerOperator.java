/**
 * 
 */
package org.aksw.deer.operators;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public interface DeerOperator {
	public List<Model> process(List<Model> models, Map<String, String> parameters);
	public List<String> getParameters();
	public List<String> getNecessaryParameters(); 
}
