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
public class MergeOperator implements DeerOperator {
	private static final Logger logger = Logger.getLogger(DereferencingModule.class.getName());

	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(final List<Model> models, final Map<String, String> parameters) {
		logger.info("--------------- Merge Operator ---------------");
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
