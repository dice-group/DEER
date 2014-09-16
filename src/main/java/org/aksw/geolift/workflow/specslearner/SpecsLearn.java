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
			Arrays.asList(
					//					new LinkingModule(),
					//					new NLPModule(),
					//					new FilterModule(),
					new ConformationModule(), 
					new DereferencingModule()));

	private int datasetCounter = 1;
	public final double childFactor = 1; 
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	private Tree<ExecutionNode> executionTreeRoot = new Tree<ExecutionNode>();
	RDFConfigWriter configWriter = new RDFConfigWriter(); 



	SpecsLearn(Model source, Model target){
		this();
		this.sourceModel  = source;
		this.targetModel  = target;
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
	long computeFitness(Model currentModel, Model targetModel){
		System.out.println("targetModel.difference(currentModel).size() = " + targetModel.difference(currentModel).size());
		System.out.println("currentModel.difference(targetModel).size() = " + currentModel.difference(targetModel).size());
		return targetModel.difference(currentModel).size() + currentModel.difference(targetModel).size();
	}

	public void initiateExecutionTree(){
		executionTreeRoot = new Tree<ExecutionNode>();
		Map<String, String> parameters = new HashMap<String, String>();

		for(GeoLiftModule module : MODULES){
			Resource inputDataset  = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
			parameters = module.selfConfig(sourceModel, targetModel);
			Model configModel = ModelFactory.createDefaultModel();
			ExecutionNode node = new ExecutionNode();
			if(parameters == null){
				// mark as dead end, fitness = -2
				configModel = ModelFactory.createDefaultModel();
				node = new ExecutionNode(module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configModel);
			}else{
				Model currentModel = module.process(sourceModel, parameters);
				long fitness = computeFitness(currentModel, targetModel);
				Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
				configModel = configWriter.addModule(ModelFactory.createDefaultModel(), module, parameters, inputDataset, outputDataset);
				node = new ExecutionNode(module, fitness, sourceModel, currentModel, inputDataset, outputDataset, configModel);
			}
			Tree<ExecutionNode> level1Node = new Tree<ExecutionNode>(node);
			executionTreeRoot.addChild(level1Node);
		}
		executionTreeRoot = addExecutionTreeLevel(executionTreeRoot);
		executionTreeRoot.print(executionTreeRoot);
		System.out.println("Min fitness Node: " + getMinFitnessNode(executionTreeRoot).getValue());
	}

	private Tree<ExecutionNode> addExecutionTreeLevel(Tree<ExecutionNode> root){
		Set<Tree<ExecutionNode>> leaves = root.getLeaves();
		for(Tree<ExecutionNode> leaf : leaves){
			for( GeoLiftModule module : MODULES){
				Map<String, String> parameters = module.selfConfig(leaf.getValue().outputModel, targetModel);
				Resource inputDataset  = leaf.getValue().outputDataset;
				Model configModel = ModelFactory.createDefaultModel();
				ExecutionNode node = new ExecutionNode();
				if(parameters == null){
					// mark as dead end, fitness = -2
					configModel = leaf.getValue().outputModel;
					node = new ExecutionNode( module, -2, sourceModel, sourceModel,inputDataset, inputDataset, configModel);
				}else{
					Model currentModel = module.process(leaf.getValue().outputModel, parameters);
					long fitness = computeFitness(currentModel, targetModel);
					Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
					configModel = configWriter.addModule(leaf.getValue().configModel, module, parameters, inputDataset, outputDataset);
					node = new ExecutionNode(module, fitness, leaf.getValue().outputModel, currentModel, inputDataset, outputDataset, configModel);
				}
				leaf.addChild(new Tree<ExecutionNode>(node));
				//				try {
				//					Writer.writeModel(leaf.getValue().configModel, "TTL", "learnerReselt_" + 0);
				//				} catch (IOException e) {
				//					e.printStackTrace();
				//				}
				//				try {
				//					Writer.writeModel(configModel, "TTL", "learnerReselt_" + 1);
				//				} catch (IOException e) {
				//					e.printStackTrace();
				//				}
			}
		}
		return root;
	}

	//	private Tree<ExecutionNode> addExecutionTreeLevel(Tree<ExecutionNode> root){
	//		Set<Tree<ExecutionNode>> leaves = root.getLeaves();
	//		for(Tree<ExecutionNode> leave : leaves){
	//			for( GeoLiftModule module : MODULES){
	//				ExecutionNode node = new ExecutionNode(module, -1, leave.getValue().output, null, leave.getValue().config);
	//				leave.addChild(new Tree<ExecutionNode>(node));
	//			}
	//		}
	//		return root;
	//	}

	Tree<ExecutionNode> getMinFitnessNode(Tree<ExecutionNode> root){
		if(root.getchildren() == null){
			return root;
		}
		Tree<ExecutionNode> result = new Tree<ExecutionNode>();
		double min = Double.MAX_VALUE; 
		for(Tree<ExecutionNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				Tree<ExecutionNode> minFitnessNode = getMinFitnessNode(child);
				double f = minFitnessNode.getValue().fitness;
				if(f < min){
					min = f;
					result = minFitnessNode;
				}
			}
		}
		return result;
	}



	public static void main(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		SpecsLearn learner = new SpecsLearn();
		learner.sourceModel  = Reader.readModel(sourceUri);
		learner.targetModel = Reader.readModel(targetUri);

		learner.initiateExecutionTree();
	}

}
