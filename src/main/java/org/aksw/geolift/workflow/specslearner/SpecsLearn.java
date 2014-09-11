/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.io.Writer;
import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.modules.Dereferencing.DereferencingModule;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.modules.linking.LinkingModule;
import org.aksw.geolift.modules.nlp.NLPModule;
import org.aksw.geolift.operators.GeoLiftOperator;
import org.aksw.geolift.operators.MergeOperator;
import org.aksw.geolift.operators.SplitOperator;
import org.aksw.geolift.workflow.rdfspecs.RDFConfigWriter;
import org.aksw.geolift.workflow.rdfspecs.SpecsOntology;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.uni_leipzig.simba.benchmarker.MergeModifier;

/**
 * @author sherif
 *
 */
public class SpecsLearn {

	private final Set<GeoLiftModule> MODULES = new HashSet<GeoLiftModule>(
			Arrays.asList(new ConformationModule(), new DereferencingModule()));

	private int datasetCounter = 1;
	public final double childFactor = 1; 
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	private RDFConfigWriter configWriter = new RDFConfigWriter();
	private Tree<ExecutionNode> executionTreeRoot = new Tree<ExecutionNode>();

	SpecsLearn(Model source, Model target){
		this();
		sourceModel  = source;
		targetModel = target;
	}

	/**
	 * 
	 *@author sherif
	 */
	public SpecsLearn() {
		sourceModel = ModelFactory.createDefaultModel();
		targetModel = ModelFactory.createDefaultModel();
	}

	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	long computeFitness(Model currentModel, int childNr){
		System.out.println("targetModel.difference(currentModel).size()" + targetModel.difference(currentModel).size());
		System.out.println("currentModel.difference(targetModel).size()" + currentModel.difference(targetModel).size());
		return targetModel.difference(currentModel).size() + currentModel.difference(targetModel).size();
	}

	public void initiateExecutionTree(Model source, Model target){
		executionTreeRoot = new Tree<ExecutionNode>();
		Map<String, String> parameters = new HashMap<String, String>();

		for(GeoLiftModule m : MODULES){
			parameters = m.selfConfig(source, target);
			Model currentModel = m.process(source, parameters);
			long fitness = computeFitness(currentModel, MODULES.size());
			Resource inputDataset = ResourceFactory.createResource(SpecsOntology.uri+"Dataset_" + datasetCounter++);
			Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri+"Dataset_" + datasetCounter++);
			Model configModel = configWriter.addModule(m, parameters, inputDataset, outputDataset);

			ExecutionNode node = new ExecutionNode( m, fitness, source, currentModel, configModel);
			Tree<ExecutionNode> level1Node = new Tree<ExecutionNode>(node);
			executionTreeRoot.addChild(level1Node);

			for( GeoLiftModule module : MODULES){
				node = new ExecutionNode(module, -1, level1Node.getValue().output, null, level1Node.getValue().config);
				Tree<ExecutionNode> level2Node = new Tree<ExecutionNode>(node);
				level1Node.addChild(level2Node);
			}
		}
		executionTreeRoot.print(executionTreeRoot);
	}



	public static void main(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		Model source  = Reader.readModel(sourceUri);
		Model target = Reader.readModel(targetUri);
		SpecsLearn learner = new SpecsLearn();
		learner.initiateExecutionTree(source, target);
	}

}
