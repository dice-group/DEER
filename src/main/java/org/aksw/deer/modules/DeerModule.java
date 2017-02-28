/**
 * 
 */
package org.aksw.deer.modules;

import java.util.List;
import java.util.Map;

import org.aksw.deer.json.ParameterType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 *
 */
public interface DeerModule {
	public Model process(Model model, Map<String, String> parameters);
	public List<String> getParameters();
	public List<String> getNecessaryParameters();
	public Map<String, String> selfConfig(Model source, Model target);
    public List<ParameterType> getParameterWithTypes();
	Resource getType();
}
