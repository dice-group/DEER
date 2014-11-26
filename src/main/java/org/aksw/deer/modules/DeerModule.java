/**
 * 
 */
package org.aksw.deer.modules;

import java.util.List;
import java.util.Map;

import org.aksw.deer.json.ParameterType;

import com.hp.hpl.jena.rdf.model.Model;

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
}
