/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.deer.helper.datastructure.TreeX;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.operators.CloneOperator;
import org.aksw.deer.operators.DeerOperator;
import org.aksw.deer.operators.OperatorFactory;
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * @author sherif
 *
 */
public class ComplexPipeLineLearner implements PipelineLearner{
	private static final Logger logger = Logger.getLogger(ComplexPipeLineLearner.class.getName());
	public double penaltyWeight = 0.5;// [0, 1]
	private int datasetCounter = 1;
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	public TreeX<RefinementNode> refinementTreeRoot = new TreeX<RefinementNode>(new RefinementNode());
	RDFConfigWriter configWriter = new RDFConfigWriter();
	public int iterationNr = 0;

	private final double 	MAX_FITNESS_THRESHOLD = 1; 
	private final long 	MAX_TREE_SIZE = 50;
	public final double 	CHILDREN_PENALTY_WEIGHT   = 1; 
	public final double 	COMPLEXITY_PENALTY_WEIGHT = 1;

	private DeerModule leftModule = null;


	/**
	 * Contractors
	 *@author sherif
	 */
	public ComplexPipeLineLearner() {
		sourceModel = ModelFactory.createDefaultModel();
		targetModel = ModelFactory.createDefaultModel();
	}

	ComplexPipeLineLearner(Model source, Model target){
		sourceModel  = source;
		targetModel  = target;
	}

	ComplexPipeLineLearner(Model source, Model target, double penaltyWeight){
		this(source, target);
		this.penaltyWeight = penaltyWeight;
	}


	public RefinementNode runComplex(){
		refinementTreeRoot = createRefinementTreeRoot();
		TreeX<RefinementNode> root = refinementTreeRoot;
		TreeX<RefinementNode> mostPromisingNode;
		do{
			TreeX<RefinementNode> tmp = root;
			List<TreeX<RefinementNode>> leftRightNodes = getLeftRightNode(tmp);
			if(leftRightNodes.size() == 1){
				root = tmp;
			}else{
				Model leftOutputModel = leftRightNodes.get(0).getValue().outputModels.get(0);
				Model rightOutputModel = leftRightNodes.get(1).getValue().outputModels.get(0);
				Model mergeModel = leftOutputModel.add(rightOutputModel);
				double mergeFitness = computeFMeasure(mergeModel, targetModel);
				double leftFitness = leftRightNodes.get(0).getValue().fitness;
				if(mergeFitness > leftFitness){
					// TODO find away not to compute lr again ?????
					TreeX<RefinementNode> cloneNode = createCloneNode(root);
					List<TreeX<RefinementNode>> lr = getLeftRightNode(cloneNode);
					root = createMergeNode(lr);
				}else{
					root = tmp;
				}
			}
			mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
			root = mostPromisingNode;
			refinementTreeRoot.print();
			mostPromisingNode.getValue().outputModels.get(0).write(System.out,"TTL");
		}while((mostPromisingNode.getValue().fitness) < MAX_FITNESS_THRESHOLD	 
				&& refinementTreeRoot.size() <= MAX_TREE_SIZE);

			return null;
	}




	private TreeX<RefinementNode> createCloneMergeNodes(TreeX<RefinementNode> root, RefinementNode leftNodeValue, RefinementNode rightNodeValue) {
		// create clone node
		TreeX<RefinementNode> cloneNode = createCloneNode(root);

		// create left and right branches
		TreeX<RefinementNode> leftNode  = new TreeX<RefinementNode>(cloneNode, leftNodeValue, null);
		TreeX<RefinementNode> rightNode = new TreeX<RefinementNode>(cloneNode, rightNodeValue, null);

		// create merge node
		TreeX<RefinementNode> mergeNode = createMergeNode(leftNode, rightNode);
		return mergeNode;
	}

//	/**
//	 * @param root
//	 * @return
//	 * @author sherif
//	 */
//	private TreeX<RefinementNode> createCloneMergeNodes(TreeX<RefinementNode> root) {
//		// create clone node
//		TreeX<RefinementNode> cloneNode = createCloneNode(root);
//
//		// create left and right branches
//		List<TreeX<RefinementNode>> leftRightNodes = getLeftRightNode(cloneNode);
//		//		TreeX<RefinementNode> leftNode 	= createLeftBranch(cloneNode);
//		//		TreeX<RefinementNode> rightNode = createRightBranch(cloneNode);
//		//		
//		//		if(leftNode == null){
//		//			return rightNode;
//		//		}else if(rightNode == null){
//		//			return leftNode;
//		//		}
//
//		// create merge node
//		//		TreeX<RefinementNode> mergeNode = createMergeNode(leftNode, rightNode);
//		TreeX<RefinementNode> mergeNode = createMergeNode(leftRightNodes);
//		return mergeNode;
//	}

//	/**
//	 * @param root
//	 * @return
//	 * @author sherif
//	 */
//	private TreeX<RefinementNode> createRightBranch(TreeX<RefinementNode> root) {
//		RefinementNode rightNodeValue = getRightNode(root);
//		TreeX<RefinementNode> rightNode = null;
//		if(rightNodeValue != null) {
//			rightNode = new TreeX<RefinementNode>(root, rightNodeValue, null);
//		}
//		return rightNode;
//	}

//	/**
//	 * @param root
//	 * @return
//	 * @author sherif
//	 */
//	private TreeX<RefinementNode> createLeftBranch(TreeX<RefinementNode> root) {
//		RefinementNode leftNodeValue = getLeftNode(root);
//		TreeX<RefinementNode> leftNode = null;
//		if(leftNodeValue != null) {
//			leftNode = new TreeX<RefinementNode>(root, leftNodeValue, null);
//		}
//		return leftNode;
//	}

	/**
	 * @param leftNodeValue
	 * @param rightNodeValue
	 * @param leftNode
	 * @param rightNode
	 * @param leftRightNodes
	 * @return
	 * @author sherif
	 */
	@SuppressWarnings("unchecked")
	private TreeX<RefinementNode> createMergeNode(TreeX<RefinementNode> leftNode, TreeX<RefinementNode> rightNode) {
		DeerOperator 	mergeOperator = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
		RefinementNode 	rightNodeValue = rightNode.getValue();
		RefinementNode 	leftNodeValue = leftNode.getValue();
		List<Model> 	mergeInputModels = new ArrayList<Model>(Arrays.asList(leftNodeValue.getOutputModel(), rightNodeValue.getOutputModel()));
		List<Resource>	mergeInputDatasets = new ArrayList<Resource>(Arrays.asList(leftNodeValue.getOutputDataset(), rightNodeValue.getOutputDataset()));
		List<Model> 	mergeOutputModels = mergeOperator.process(mergeInputModels, null);
		List<Model> 	mergeInputConfig = new ArrayList<Model>(Arrays.asList(leftNodeValue.configModel, rightNodeValue.configModel));
		List<Resource> 	mergeOutputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));
		Model 			mergeConfigModel = configWriter.addOperator(mergeOperator, null, mergeInputConfig , mergeInputDatasets, mergeOutputDatasets);
		double 			fitness = computeFMeasure(mergeOutputModels.get(0), targetModel);
		RefinementNode mergeNodeValue = new RefinementNode(mergeOperator, fitness, mergeInputModels, mergeOutputModels, mergeConfigModel, mergeInputDatasets, mergeOutputDatasets);
		List<TreeX<RefinementNode>> leftRightNodes = new ArrayList<TreeX<RefinementNode>>(Arrays.asList(leftNode, rightNode));
		TreeX<RefinementNode> mergeNode = new TreeX<RefinementNode>(leftRightNodes ,mergeNodeValue, (TreeX<RefinementNode>) null);
		return mergeNode;
	}

	private TreeX<RefinementNode> createMergeNode(List<TreeX<RefinementNode>> leftrightNodes) {
//		if(leftrightNodes.size() == 0){
//			return null;
//		}
//		if(leftrightNodes.size() == 1){
//			return leftrightNodes.get(0);
//		}
		DeerOperator 	mergeOperator = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
		RefinementNode 	rightNodeValue = leftrightNodes.get(1).getValue();
		RefinementNode 	leftNodeValue = leftrightNodes.get(0).getValue();
		List<Model> 	mergeInputModels = new ArrayList<Model>(Arrays.asList(leftNodeValue.getOutputModel(), rightNodeValue.getOutputModel()));
		List<Resource>	mergeInputDatasets = new ArrayList<Resource>(Arrays.asList(leftNodeValue.getOutputDataset(), rightNodeValue.getOutputDataset()));
		List<Model> 	mergeOutputModels = mergeOperator.process(mergeInputModels, null);
		List<Model> 	mergeInputConfig = new ArrayList<Model>(Arrays.asList(leftNodeValue.configModel, rightNodeValue.configModel));
		List<Resource> 	mergeOutputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));
		Model 			mergeConfigModel = configWriter.addOperator(mergeOperator, null, mergeInputConfig , mergeInputDatasets, mergeOutputDatasets);
		double 			fitness = computeFMeasure(mergeOutputModels.get(0), targetModel);
		RefinementNode mergeNodeValue = new RefinementNode(mergeOperator, fitness, mergeInputModels, mergeOutputModels, mergeConfigModel, mergeInputDatasets, mergeOutputDatasets);
		List<TreeX<RefinementNode>> leftRightNodes = new ArrayList<TreeX<RefinementNode>>(Arrays.asList(leftrightNodes.get(0), leftrightNodes.get(1)));
		TreeX<RefinementNode> mergeNode = new TreeX<RefinementNode>(leftRightNodes ,mergeNodeValue, (TreeX<RefinementNode>) null);
		return mergeNode;
	}

	/**
	 * @param root
	 * @return
	 * @author sherif
	 */
	private TreeX<RefinementNode> createCloneNode(TreeX<RefinementNode> root) {
		DeerOperator cloneOperator	  = OperatorFactory.createOperator(OperatorFactory.CLONE_OPERATOR);
		List<Model> cloneInputModels  = root.getValue().outputModels;
		List<Model> cloneOutputModels = cloneOperator.process(cloneInputModels, null);
		List<Resource> cloneInputDatasets  = root.getValue().outputDatasets;
		List<Resource> cloneOutputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI(), generateDatasetURI()));
		List<Model> cloneInputConfig 	   = new ArrayList<Model>(Arrays.asList(root.getValue().configModel));
		Model cloneConfigModel = configWriter.addOperator(cloneOperator, null, cloneInputConfig , cloneInputDatasets, cloneOutputDatasets);
		RefinementNode cloneNodeValue = new RefinementNode(cloneOperator, -1, cloneInputModels, cloneOutputModels, cloneConfigModel, cloneInputDatasets, cloneOutputDatasets);
		TreeX<RefinementNode> cloneNode = new TreeX<RefinementNode>(root ,cloneNodeValue, null);
		return cloneNode;
	}


	public RefinementNode run(){
		refinementTreeRoot = createRefinementTreeRoot();
		refinementTreeRoot = expandNode(refinementTreeRoot);
		TreeX<RefinementNode> mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
		refinementTreeRoot.print();
		logger.info("Most promising node: " + mostPromisingNode.getValue());
		iterationNr ++;
		while((mostPromisingNode.getValue().fitness) < MAX_FITNESS_THRESHOLD	 
				&& refinementTreeRoot.size() <= MAX_TREE_SIZE)
		{
			iterationNr++;
			mostPromisingNode = expandNode(mostPromisingNode);
			mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
			refinementTreeRoot.print();
			if(mostPromisingNode.getValue().fitness == -Double.MAX_VALUE){
				// no better solution can be found
				break;
			}
			logger.info("Most promising node: " + mostPromisingNode.getValue());
		}
		logger.info("----------------------------------------------");
		RefinementNode bestSolution = getMostPromisingNode(refinementTreeRoot, 0).getValue();
		//		logger.info("Best Solution: " + bestSolution.toString());
		//		System.out.println("===== Output Config =====");
		//		bestSolution.configModel.write(System.out,"TTL");
		//		System.out.println("===== Output Dataset =====");
		//		bestSolution.outputModel.write(System.out,"TTL");
		//		System.out.println("===== Output Config =====");
		//		mostPromisingNode.getValue().configModel.write(System.out,"TTL");
		//		System.out.println("===== Output Dataset =====");
		//		mostPromisingNode.getValue().outputModel.write(System.out,"TTL");
		bestSolution.configModel = setIOFiles(bestSolution.configModel, "inputFile.ttl", "outputFile.ttl"); 
		return bestSolution;
	}

	private TreeX<RefinementNode> createRefinementTreeRoot(){
		Resource outputDataset  = generateDatasetURI();
		Model config = ModelFactory.createDefaultModel();
		double f = -Double.MAX_VALUE;
		RefinementNode initialNode = new RefinementNode(null, f, sourceModel, sourceModel,config,outputDataset,outputDataset);
		return new TreeX<RefinementNode>((TreeX<RefinementNode>)null,initialNode, null);
	}

	private TreeX<RefinementNode> expandNode(TreeX<RefinementNode> root) {
		for( DeerModule module : MODULES){
			Model inputModel = root.getValue().getOutputModel();
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().getOutputDataset();
			Model configModel = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				// mark as dead end, fitness = -2
				configModel = root.getValue().configModel;
				node = new RefinementNode(module, -2, sourceModel, sourceModel,configModel, inputDataset, inputDataset);
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				double fitness;
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					fitness = -2;
				}else{
					//					fitness = computeFitness(currentMdl, targetModel);
					fitness = computeFMeasure(currentMdl, targetModel);
				}
				Resource outputDataset = generateDatasetURI();
				configModel = configWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, root.getValue().getOutputModel(), currentMdl, configModel, inputDataset, outputDataset);
			}
			root.addChild(new TreeX<RefinementNode>(node));
		}
		return root;
	}

	private RefinementNode getLeftNode(TreeX<RefinementNode> root) {
		RefinementNode promisingNode = null; 
		for( DeerModule module : MODULES){
			Model inputModel = root.getValue().getOutputModel();
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				continue; // Dead node
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					continue; // Dead node
				}else{
					double fitness = computeFMeasure(currentMdl, targetModel);
					Resource outputDataset = generateDatasetURI();
					Resource inputDataset  = root.getValue().outputDatasets.get(0);
					Model configModel = configWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
					RefinementNode node = new RefinementNode(module, fitness, root.getValue().getOutputModel(), currentMdl, configModel, inputDataset, outputDataset);
					if(promisingNode == null || promisingNode.fitness < fitness){
						promisingNode = node;
						leftModule = module;
					}
				}
			}
		}
		//		root.addChild(new TreeX<RefinementNode>(promisingNode));
		System.err.println("getLeftNode: " + promisingNode);
		return promisingNode;
	}




	private RefinementNode getRightNode(TreeX<RefinementNode> root) {
		RefinementNode promisingNode = null; 
		for( DeerModule module : MODULES){
			if(module.getClass().equals(leftModule.getClass())){
				continue;
			}
			Model inputModel = root.getValue().getOutputModel();
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				continue; // Dead node
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					continue; // Dead node
				}else{
					double fitness = computeFMeasure(currentMdl, targetModel);
					Resource outputDataset = generateDatasetURI();
					Resource inputDataset  = root.getValue().outputDatasets.get(0);
					Model configModel = configWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, root.getValue().getOutputModel(), currentMdl, configModel, inputDataset, outputDataset);
					if(promisingNode == null || promisingNode.fitness < fitness){
						promisingNode = node;
					}
				}
			}
		}
		//		root.addChild(new TreeX<RefinementNode>(promisingNode));
		System.err.println("getRightNode: " + promisingNode);
		return promisingNode;
	}

	@SuppressWarnings("unchecked")
	private List<TreeX<RefinementNode>> getLeftRightNode(TreeX<RefinementNode> root) {
		RefinementNode leftNodeValue = null;
		RefinementNode rightNodeValue = null;
		for( DeerModule module : MODULES){
			Model inputModel = root.getValue().getOutputModel();
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				continue; // Dead node
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					continue; // Dead node
				}else{
					double fitness = computeFMeasure(currentMdl, targetModel);
					Resource outputDataset = generateDatasetURI();
					Resource inputDataset  = root.getValue().outputDatasets.get(0);
					Model configModel = configWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
					node = new RefinementNode(module, fitness, root.getValue().getOutputModel(), currentMdl, configModel, inputDataset, outputDataset);
					if(leftNodeValue == null || leftNodeValue.fitness < fitness){
						rightNodeValue = leftNodeValue;
						leftNodeValue = node;
					}else if(rightNodeValue == null || rightNodeValue.fitness < fitness){
						rightNodeValue = node;
					}
				}
			}
		}
		if(leftNodeValue == null){
			return new ArrayList<TreeX<RefinementNode>>(
					Arrays.asList(new TreeX<RefinementNode>(root, rightNodeValue, null)));
		}else if(rightNodeValue == null){
			return new ArrayList<TreeX<RefinementNode>>(
					Arrays.asList(new TreeX<RefinementNode>(root, leftNodeValue, null)));
		}
		return new ArrayList<TreeX<RefinementNode>>(
				Arrays.asList(new TreeX<RefinementNode>(root, leftNodeValue, null), 
						new TreeX<RefinementNode>(root, rightNodeValue, null)));
	}


	/**
	 * Compute the fitness of the generated model by current specs
	 * Simple implementation is difference between current and target 
	 * @return
	 * @author sherif
	 */
	double computeFitness(Model currentModel, Model targetModel){
		long t_c = targetModel.difference(currentModel).size();
		long c_t = currentModel.difference(targetModel).size();
		System.out.println("targetModel.difference(currentModel).size() = " + t_c);
		System.out.println("currentModel.difference(targetModel).size() = " + c_t);
		return 1- ((double)(t_c + c_t) / (double)(currentModel.size() + targetModel.size()));
	}

	double computeFMeasure(Model currentModel, Model targetModel){
		double p = computePrecision(currentModel, targetModel);
		double r = computeRecall(currentModel, targetModel);
		if(p == 0 && r == 0){
			return 0;
		}
		return 2 * p * r / (p +r);

	}

	double computePrecision (Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) currentModel.size();
	}

	double computeRecall(Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) targetModel.size();
	}

	private TreeX<RefinementNode> getMostPromisingNode(TreeX<RefinementNode> root, double penaltyWeight){
		// trivial case
		if(root.getchildren() == null || root.getchildren().size() == 0){
			return root;
		}
		// get mostPromesyChild of children
		TreeX<RefinementNode> mostPromesyChild = new TreeX<RefinementNode>(new RefinementNode());
		for(TreeX<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				TreeX<RefinementNode> promesyChild = getMostPromisingNode(child, penaltyWeight);
				double newFitness;
				newFitness = promesyChild.getValue().fitness - penaltyWeight * computePenality(promesyChild);
				if( newFitness > mostPromesyChild.getValue().fitness  ){
					mostPromesyChild = promesyChild;
				}
			}
		}
		// return the argmax{root, mostPromesyChild}
		if(penaltyWeight > 0){
			return mostPromesyChild;
		}else if(root.getValue().fitness >= mostPromesyChild.getValue().fitness){
			return root;
		}else{
			return mostPromesyChild;
		}
	}


	/**
	 * @return
	 * @author sherif
	 */
	private double computePenality(TreeX<RefinementNode> promesyChild) {
		long childrenCount = promesyChild.size() - 1;
		double childrenPenalty = (CHILDREN_PENALTY_WEIGHT * childrenCount) / refinementTreeRoot.size();
		long level = promesyChild.level();
		double complextyPenalty = (COMPLEXITY_PENALTY_WEIGHT * level) / refinementTreeRoot.depth();
		return  childrenPenalty + complextyPenalty;
	}


	public static void trivialRun(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		ComplexPipeLineLearner learner = new ComplexPipeLineLearner();
		learner.sourceModel  = Reader.readModel(sourceUri);
		learner.targetModel = Reader.readModel(targetUri);
		long start = System.currentTimeMillis();
		learner.runComplex();
		long end = System.currentTimeMillis();
		logger.info("Done in " + (end - start) + "ms");
	}

	public static void evaluation(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];
		String results = "ModuleCount\tTime\tTreeSize\tIterationNr\tP\tR\tF\n";
		for(int i = 1 ; i <= max; i++){
			ComplexPipeLineLearner learner = new ComplexPipeLineLearner();
			if(isBatch){
				folder = folder + i;
			}
			learner.sourceModel  = Reader.readModel(folder + "/input.ttl");
			learner.targetModel  = Reader.readModel(folder + "/output.ttl");
			long start = System.currentTimeMillis();
			RefinementNode bestSolution = learner.run();
			long end = System.currentTimeMillis();
			long time = end - start;
			results += i + "\t" + time + "\t" + 
					learner.refinementTreeRoot.size() + "\t" + 
					learner.iterationNr + "\t" + 
					//					bestSolution.fitness + "\t" +
					learner.computePrecision(bestSolution.getOutputModel(), targetModel) + "\t" + 
					learner.computeRecall(bestSolution.getOutputModel(), targetModel) + "\t" +
					learner.computeFMeasure(bestSolution.getOutputModel(), targetModel);
			Writer.writeModel(bestSolution.configModel, "TTL", folder + "/self_config.ttl");
			//			bestSolution.outputModel.write(System.out,"TTL");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(results);
			//			break;
		}
		System.out.println(results);
	}


	Model setIOFiles(final Model sConfig, String inputFile, String outputFile){
		Model resultModel = ModelFactory.createDefaultModel();
		resultModel = resultModel.union(sConfig);
		List<String> datasets = new ArrayList<String>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?d <" + RDF.type + "> <" + SPECS.Dataset + ">.} ";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, resultModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			datasets.add(dataset.toString());
		}
		qexec.close() ;
		Collections.sort(datasets);
		Resource inputDataset = ResourceFactory.createResource(datasets.get(0));
		Resource outputDataset = ResourceFactory.createResource(datasets.get(datasets.size()-1));
		resultModel.add(inputDataset, SPECS.inputFile, inputFile);
		resultModel.add(outputDataset, SPECS.outputFile, outputFile);
		resultModel.setNsPrefixes(sConfig);
		return resultModel;
	}


	private Resource generateDatasetURI() {
		return ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
	}

	public static void main(String args[]) throws IOException{
		trivialRun(args);
		//evaluation(args, false, 1);
	}


}
