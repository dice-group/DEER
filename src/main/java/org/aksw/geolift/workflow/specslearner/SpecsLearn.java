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
import org.apache.log4j.Logger;

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
	private static final Logger logger = Logger.getLogger(SpecsLearn.class.getName());

	private final Set<GeoLiftModule> MODULES = new HashSet<GeoLiftModule>(
			Arrays.asList(
					//					new LinkingModule(),
					//					new NLPModule(),
															new FilterModule()));
//					new ConformationModule(), 
//					new DereferencingModule()));

	private int datasetCounter = 1;

	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	private Tree<RefinementNode> executionTreeRoot = new Tree<RefinementNode>(new RefinementNode());
	RDFConfigWriter configWriter = new RDFConfigWriter();

	private final double MIN_FITNESS_THRESHOLD = 0; 
	private final float LEAVES_EXPANSION_FACTOR = 0.1f;
	private final long MAX_TREE_SIZE = 100;
	public final double CHILDREN_MULTIPLIER = 5; 





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

	public void run(){
		initiateExecutionTree();
		executionTreeRoot.print(executionTreeRoot);
		Tree<RefinementNode> minFitnessNode = getMinFitnessNode(executionTreeRoot);
		
		while(minFitnessNode.getValue().fitness > MIN_FITNESS_THRESHOLD 
				&& executionTreeRoot.size() < MAX_TREE_SIZE){
			minFitnessNode = expandNode(minFitnessNode);
//			updateParentsFitness(minFitnessNode);
			minFitnessNode = getMinFitnessNode(executionTreeRoot);
			executionTreeRoot.print(executionTreeRoot);
		}
		executionTreeRoot.print(executionTreeRoot);
		logger.info("Min fitness Node: " + getMinFitnessNode(executionTreeRoot).getValue());
		System.out.println("===== Output Config =====");
		minFitnessNode.getValue().configModel.write(System.out,"TTL");
		System.out.println("===== Output Dataset =====");
		minFitnessNode.getValue().outputModel.write(System.out,"TTL");
	}


	private void updateParentsFitness(	Tree<RefinementNode> root) {
		while(root != null){
			long rootChildrenCount = root.size() - 1;
			root.getValue().fitness += CHILDREN_MULTIPLIER * rootChildrenCount;
			root = root.getParent();
		}
	}

	/**
	 * @param minFitnessNode
	 * @author sherif
	 */
	private Tree<RefinementNode> expandNode(Tree<RefinementNode> root) {
		for( GeoLiftModule module : MODULES){
			Map<String, String> parameters = module.selfConfig(root.getValue().outputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configModel = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			if(parameters == null){
				// mark as dead end, fitness = -2
				configModel = root.getValue().outputModel;
				node = new RefinementNode( module, -2, sourceModel, sourceModel,inputDataset, inputDataset, configModel);
			}else{
				Model currentModel = module.process(root.getValue().outputModel, parameters);
				double fitness = computeFitness(currentModel, targetModel);
				Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
				configModel = configWriter.addModule(root.getValue().configModel, module, parameters, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, root.getValue().outputModel, currentModel, inputDataset, outputDataset, configModel);
			}
			root.addChild(new Tree<RefinementNode>(node));
		}
		return root;
	}

	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	double computeFitness(Model currentModel, Model targetModel){
		System.out.println("targetModel.difference(currentModel).size() = " + targetModel.difference(currentModel).size());
		System.out.println("currentModel.difference(targetModel).size() = " + currentModel.difference(targetModel).size());
		return targetModel.difference(currentModel).size() + currentModel.difference(targetModel).size();
	}

	private Tree<RefinementNode> initiateExecutionTree(){
		Map<String, String> parameters = new HashMap<String, String>();

		for(GeoLiftModule module : MODULES){
			Resource inputDataset  = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
			parameters = module.selfConfig(sourceModel, targetModel);
			Model configModel = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			if(parameters == null){
				// mark as dead end, fitness = -2
				configModel = ModelFactory.createDefaultModel();
				node = new RefinementNode(module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configModel);
			}else{
				Model currentModel = module.process(sourceModel, parameters);
				double fitness = computeFitness(currentModel, targetModel);
				Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
				configModel = configWriter.addModule(ModelFactory.createDefaultModel(), module, parameters, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, sourceModel, currentModel, inputDataset, outputDataset, configModel);
			}
			Tree<RefinementNode> level1Node = new Tree<RefinementNode>(node);
			executionTreeRoot.addChild(level1Node);
		}
		return executionTreeRoot;
		//		executionTreeRoot = addExecutionTreeLevel(executionTreeRoot);
		//		executionTreeRoot.print(executionTreeRoot);
		//		System.out.println("Min fitness Node: " + getMinFitnessNode(executionTreeRoot).getValue());
	}

	private Tree<RefinementNode> expandLeaves(Tree<RefinementNode> root){
		Set<Tree<RefinementNode>> leaves = root.getLeaves();
		for(Tree<RefinementNode> leaf : leaves){
			for( GeoLiftModule module : MODULES){
				Map<String, String> parameters = module.selfConfig(leaf.getValue().outputModel, targetModel);
				Resource inputDataset  = leaf.getValue().outputDataset;
				Model configModel = ModelFactory.createDefaultModel();
				RefinementNode node = new RefinementNode();
				if(parameters == null){
					// mark as dead end, fitness = -2
					configModel = leaf.getValue().outputModel;
					node = new RefinementNode( module, -2, sourceModel, sourceModel,inputDataset, inputDataset, configModel);
				}else{
					Model currentModel = module.process(leaf.getValue().outputModel, parameters);
					double fitness = computeFitness(currentModel, targetModel);
					Resource outputDataset = ResourceFactory.createResource(SpecsOntology.uri + "Dataset_" + datasetCounter++);
					configModel = configWriter.addModule(leaf.getValue().configModel, module, parameters, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, leaf.getValue().outputModel, currentModel, inputDataset, outputDataset, configModel);
				}
				Tree<RefinementNode> child = new Tree<RefinementNode>(node);
				child.setStatus(Status.DEAD);
				leaf.addChild(child);
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


	private Tree<RefinementNode> getMinFitnessNode(Tree<RefinementNode> root){
		// trivial case
		if(root.getchildren() == null){
			return root;
		}
		// get minChild of children
		Tree<RefinementNode> minChild = new Tree<RefinementNode>(new RefinementNode());
		for(Tree<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				Tree<RefinementNode> minFintnessChild = getMinFitnessNode(child);
				double newFitness = minFintnessChild.getValue().fitness + CHILDREN_MULTIPLIER * (minFintnessChild.size() - 1);
				if( newFitness < minChild.getValue().fitness  ){
					minChild = minFintnessChild;
				}
			}
		}
		// return the min{root, minChild}
		if(root.getValue().fitness <= minChild.getValue().fitness){
			return root;
		}else{
			return minChild;
		}
	}

	private void updateFitness(Tree<RefinementNode> root, double fitness){
		if(root.getParent() == null){
			return;
		}
		root.getValue().fitness += fitness;
	}



	public static void main(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		SpecsLearn learner = new SpecsLearn();
		learner.sourceModel  = Reader.readModel(sourceUri);
		learner.targetModel = Reader.readModel(targetUri);

		learner.run();
	}

}
