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

import org.aksw.geolift.helper.vacabularies.SPECS;
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

	private final Set<GeoLiftModule> MODULES = 
			new HashSet<GeoLiftModule>(Arrays.asList(
					new LinkingModule(),
					new NLPModule(),
					new FilterModule(),
					new ConformationModule(), 
					new DereferencingModule()
					));

	private int datasetCounter = 1;
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	private Tree<RefinementNode> refinementTreeRoot = new Tree<RefinementNode>(new RefinementNode());
	RDFConfigWriter configWriter = new RDFConfigWriter();

	private final double 	MIN_FITNESS_THRESHOLD = 0; 
	private final long 	MAX_TREE_SIZE = 50;
	public final double 	CHILDREN_PENALTY_WEIGHT   = 1; 
	public final double 	COMPLEXITY_PENALTY_WEIGHT = 1;





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
		//		initiateExecutionTree();
		Resource outputDataset  = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
		Model config = ModelFactory.createDefaultModel();
		double f = Double.MAX_VALUE;
		RefinementNode initialNode = new RefinementNode(null,f,sourceModel,sourceModel,outputDataset,outputDataset,config);
		refinementTreeRoot = new Tree<RefinementNode>(null,initialNode, null);
		refinementTreeRoot = expandNode(refinementTreeRoot);

		Tree<RefinementNode> minFitnessNode = getMostPromesyNode(refinementTreeRoot, true);
//		refinementTreeRoot.print(refinementTreeRoot);
//		System.out.println("+++++++++++++++++++++++");
		refinementTreeRoot.print();
		logger.info("Min fitness Node: " + minFitnessNode.getValue());

		while(minFitnessNode.getValue().fitness > MIN_FITNESS_THRESHOLD	){
			if(refinementTreeRoot.size() >= MAX_TREE_SIZE){
				logger.info("----------------------------------------------");
				RefinementNode bestSolution = getMostPromesyNode(refinementTreeRoot, false).getValue();
				logger.info("Best Solution: " + bestSolution.toString());
				System.out.println("===== Output Config =====");
				bestSolution.configModel.write(System.out,"TTL");
				System.out.println("===== Output Dataset =====");
				bestSolution.outputModel.write(System.out,"TTL");
				return;
			}
			minFitnessNode = expandNode(minFitnessNode);
			//			updateParentsFitness(minFitnessNode);
			minFitnessNode = getMostPromesyNode(refinementTreeRoot, true);
//			refinementTreeRoot.print(refinementTreeRoot);
			refinementTreeRoot.print();
			logger.info("Min fitness Node: " + minFitnessNode.getValue());

		}
		System.out.println("===== Output Config =====");
		minFitnessNode.getValue().configModel.write(System.out,"TTL");
		System.out.println("===== Output Dataset =====");
		minFitnessNode.getValue().outputModel.write(System.out,"TTL");
	}


	private void updateParentsFitness(	Tree<RefinementNode> root) {
		while(root != null){
			long rootChildrenCount = root.size() - 1;
			root.getValue().fitness += CHILDREN_PENALTY_WEIGHT * rootChildrenCount;
			root = root.getParent();
		}
	}

	private Tree<RefinementNode> initiateExecutionTree(){
		Map<String, String> parameters = new HashMap<String, String>();
		for(GeoLiftModule module : MODULES){
			Resource inputDataset  = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
			Model configModel = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			parameters = module.selfConfig(sourceModel, targetModel);
			if(parameters == null || parameters.size() == 0){
				// mark as dead end, fitness = -2
				configModel = ModelFactory.createDefaultModel();
				node = new RefinementNode(module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configModel);
			}else{
				Model currentModel = module.process(sourceModel, parameters);
				double fitness;
				if(currentModel == null || currentModel.size() == 0){
					fitness = -2;
				}else{
					fitness = computeFitness(currentModel, targetModel);
				}
				Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
				configModel = configWriter.addModule(ModelFactory.createDefaultModel(), module, parameters, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, sourceModel, currentModel, inputDataset, outputDataset, configModel);
			}
			Tree<RefinementNode> level1Node = new Tree<RefinementNode>(node);
			refinementTreeRoot.addChild(level1Node);
		}
		return refinementTreeRoot;
	}

	private Tree<RefinementNode> expandNode(Tree<RefinementNode> root) {
		for( GeoLiftModule module : MODULES){
			Model inputModel = root.getValue().outputModel;
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configMdl = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			if(parameters == null || parameters.size() == 0){
				// mark as dead end, fitness = -2
				configMdl = root.getValue().configModel;
				node = new RefinementNode( module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configMdl);
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				double fitness;
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.remove(inputModel).size() == 0){
					fitness = -2;
				}else{
					fitness = computeFitness(currentMdl, targetModel);
				}
				Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
				configMdl = configWriter.addModule(root.getValue().configModel, module, parameters, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, root.getValue().outputModel, currentMdl, inputDataset, outputDataset, configMdl);
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
					double fitness;
					if(currentModel == null || currentModel.size() == 0){
						fitness = -2;
					}else{
						fitness = computeFitness(currentModel, targetModel);
					}
					Resource outputDataset = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
					configModel = configWriter.addModule(leaf.getValue().configModel, module, parameters, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, leaf.getValue().outputModel, currentModel, inputDataset, outputDataset, configModel);
				}
				Tree<RefinementNode> child = new Tree<RefinementNode>(node);
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


	private Tree<RefinementNode> getMostPromesyNode(Tree<RefinementNode> root, boolean usePenalty){
		// trivial case
		if(root.getchildren() == null || root.getchildren().size() == 0){
			return root;
		}
		// get mostPromesyChild of children
		Tree<RefinementNode> mostPromesyChild = new Tree<RefinementNode>(new RefinementNode());
		for(Tree<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				Tree<RefinementNode> promesyChild = getMostPromesyNode(child, usePenalty);
				double newFitness;
				if(usePenalty){
					long childrenCount = promesyChild.size() - 1;
					double childrenPenalty = CHILDREN_PENALTY_WEIGHT * childrenCount;
					long complexty = promesyChild.level();
					double complextyPenalty = COMPLEXITY_PENALTY_WEIGHT * complexty;
					newFitness = promesyChild.getValue().fitness + childrenPenalty + complextyPenalty;
				}else{
					newFitness = promesyChild.getValue().fitness;
				}
				if( newFitness < mostPromesyChild.getValue().fitness  ){
					mostPromesyChild = promesyChild;
				}
			}
		}
		// return the min{root, mostPromesyChild}
		if(usePenalty){
			return mostPromesyChild;
		}else if(root.getValue().fitness <= mostPromesyChild.getValue().fitness){
			return root;
		}else{
			return mostPromesyChild;
		}
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
