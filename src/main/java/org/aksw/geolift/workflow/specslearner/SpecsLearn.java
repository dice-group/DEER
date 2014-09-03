/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.ArrayList;
import java.util.List;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.operators.GeoLiftOperator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class SpecsLearn {
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	
	List<GeoLiftModule>	  modulesList;
	List<GeoLiftOperator> operatorList;
	
	SpecsLearn(Model start, Model end){
		sourceModel  = start;
		targetModel = end;
		modulesList  = new ArrayList<GeoLiftModule>();
		modulesList.add(new ConformationModule());
		modulesList.add(new FilterModule());
		operatorList = new ArrayList<GeoLiftOperator>();
	}
	
	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	long computeFitness(Model currentModel){
		return targetModel.difference(currentModel).size();
	}
	
	
	public static void main(String args[]){
		String initialUri = args[0];
		String enrichedUri = args[1];
		sourceModel  = Reader.readModel(initialUri);
		targetModel = Reader.readModel(enrichedUri);
		Model diffModel = targetModel.difference(sourceModel);
		
		System.out.println("------------ initial Model ------------");
//		initialModel.write(System.out, "TTL");
		System.out.println(sourceModel.size());
		
		System.out.println("------------ enriched Model ------------");
//		enrichedModel.write(System.out, "TTL");
		System.out.println(targetModel.size());
		
		System.out.println("------------ Diff Model ------------");
//		diffModel.write(System.out, "TTL");
		System.out.println(diffModel.size());
	}

}
