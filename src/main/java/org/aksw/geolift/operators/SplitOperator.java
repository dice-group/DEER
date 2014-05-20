/**
 * 
 */
package org.aksw.geolift.operators;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class SplitOperator implements ModelOperator {
	public int splitsCount = 2;
	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> run(List<Model> models) {
		List<Model> result = new ArrayList<Model>();
		for (int i=0; i<splitsCount ; i++) {
			Model split = ModelFactory.createDefaultModel();
			split.add(models.get(0));
			result.add(split);
		}
		return result;
	}
	
	public List<Model> run(List<Model> models, int n) {
		setSplitsCount(n);
		return run(models);
	}
	
	public void setSplitsCount(int n){
		splitsCount = n;
	}

}
