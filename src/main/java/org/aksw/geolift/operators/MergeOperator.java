/**
 * 
 */
package org.aksw.geolift.operators;

import java.util.ArrayList;
import java.util.List;

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
	public List<Model> run(List<Model> models) {
		List<Model> result = new ArrayList<Model>();
		Model merge = ModelFactory.createDefaultModel();
		for (Model model : models) {
			merge.add(model);
		}
		result.add(merge);
		return result;
	}

}
