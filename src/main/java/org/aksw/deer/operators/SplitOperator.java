/**
 * 
 */
package org.aksw.deer.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class SplitOperator implements DeerOperator {
	private static final Logger logger = Logger.getLogger(DereferencingModule.class.getName());
	public int splitsCount = 2;
	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(List<Model> models, Map<String, String> parameters) {
		logger.info("--------------- Split Operator ---------------");
		if(parameters.containsKey("splitsCount")){
			splitsCount = Integer.parseInt(parameters.get("splitsCount"));
		}
		List<Model> result = new ArrayList<Model>();
		for (int i=0; i<splitsCount ; i++) {
			Model split = ModelFactory.createDefaultModel();
			split.add(models.get(0));
			result.add(split);
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("splitsCount");
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
