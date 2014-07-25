/**
 * 
 */
package org.aksw.geolift.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class MergeOperator implements ModelOperator {

	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(List<Model> models, Map<String, String> parameters) {
		List<Model> result = new ArrayList<Model>();
		Model merge = ModelFactory.createDefaultModel();
		for (Model model : models) {
			merge.add(model);
		}
		result.add(merge);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		return parameters;
	}
	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		return parameters;
	}

}
