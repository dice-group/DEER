/**
 * 
 */
package org.aksw.deer.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class CloneOperator implements DeerOperator {
	private static final Logger logger = Logger.getLogger(CloneOperator.class.getName());
	public int clonesCount = 2;
	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(final List<Model> models,final Map<String, String> parameters) {
		logger.info("--------------- Clone Operator ---------------");
		if(parameters != null && parameters.containsKey("cloneCount")){
			clonesCount = Integer.parseInt(parameters.get("cloneCount"));
		}
		List<Model> result = new ArrayList<Model>();
		for (int i=0; i < clonesCount ; i++) {
			Model clone = ModelFactory.createDefaultModel();
			clone.add(models.get(0));
			result.add(clone);
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("cloneCount");
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
