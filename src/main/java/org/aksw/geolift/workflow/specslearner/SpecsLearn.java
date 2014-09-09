/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.modules.Dereferencing.DereferencingModule;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.modules.linking.LinkingModule;
import org.aksw.geolift.modules.nlp.NLPModule;
import org.aksw.geolift.operators.GeoLiftOperator;
import org.aksw.geolift.operators.MergeOperator;
import org.aksw.geolift.operators.SplitOperator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.uni_leipzig.simba.benchmarker.MergeModifier;

/**
 * @author sherif
 *
 */
public class SpecsLearn {
	public final double childFactor = 1; 
	
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	
	List<GeoLiftModule>	  modulesList;
	List<GeoLiftOperator> operatorList;
	TreeSet<ExecutionNode> executionNodes = new TreeSet<ExecutionNode>();
	
	SpecsLearn(Model source, Model target){
		sourceModel  = source;
		targetModel = target;
		modulesList  = new ArrayList<GeoLiftModule>();
		
		modulesList.add(new ConformationModule());
//		modulesList.add(new NlpModule());
//		modulesList.add(new DereferencingModule());
//		modulesList.add(new LinkingModule());
//		modulesList.add(new FilterModule());
		
		operatorList = new ArrayList<GeoLiftOperator>();
//		operatorList.add(new SplitOperator());
//		operatorList.add(new MergeOperator());
	}
	
	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	long computeFitness(Model currentModel, int childNr){
		return targetModel.difference(currentModel).size();
	}
	
	public Model generateConfigFile(Model source, Model target){
		Model result = ModelFactory.createDefaultModel();
		for(GeoLiftModule m : modulesList){
			if(m instanceof ConformationModule){
				m = new ConformationModule(source, target);
			}
			Model currentModel = m.process(source, null);
			long fitness = computeFitness(currentModel, modulesList.size());
			executionNodes.add(new ExecutionNode(m, fitness, currentModel, modulesList.size()));
		}
		
		
		return result;
	}
	
	
	public static void main(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		sourceModel  = Reader.readModel(sourceUri);
		targetModel = Reader.readModel(targetUri);
		ConformationModule c = new ConformationModule(sourceModel, targetModel);
		
		
//		Model diffModel = targetModel.difference(sourceModel);
//		
//		System.out.println("------------ initial Model ------------");
////		initialModel.write(System.out, "TTL");
//		System.out.println(sourceModel.size());
//		
//		System.out.println("------------ enriched Model ------------");
////		enrichedModel.write(System.out, "TTL");
//		System.out.println(targetModel.size());
//		
//		System.out.println("------------ Diff Model ------------");
////		diffModel.write(System.out, "TTL");
//		System.out.println(diffModel.size());
	}

}
