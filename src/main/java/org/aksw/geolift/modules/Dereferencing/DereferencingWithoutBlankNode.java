/**
 * 
 */
package org.aksw.geolift.modules.Dereferencing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author sherif
 *
 */
public class DereferencingWithoutBlankNode {
	
	
	public Model process(Model model, Map<String, String> parameters){
		List<Property> enrichmentProperties = getEnrichmentProperties(parameters);
		return model;
		
	}

	/**
	 * @param parameters
	 * @return
	 * @author sherif
	 */
	private List<Property> getEnrichmentProperties(	Map<String, String> parameters) {
		List<Property> result = new ArrayList<Property>();
		for(String p: parameters.keySet()){
			if(p.toLowerCase().startsWith("properity")){
				result.add(ResourceFactory.createProperty(parameters.get(p)));
			}
		}
		return null;
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
