/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.datastructure.TreeX;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.operators.DeerOperator;
import org.aksw.deer.operators.OperatorFactory;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
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

	public ComplexPipeLineLearner(Model source, Model target){
		sourceModel  = source;
		targetModel  = target;
	}

	public ComplexPipeLineLearner(Model source, Model target, double penaltyWeight){
		this(source, target);
		this.penaltyWeight = penaltyWeight;
	}


	/**
	 * Learn specification with both modules (enrichment functions) and operators
	 * @return the RefinementNode containing the best found solution of the refinement tree
	 * @author sherif
	 */
	public RefinementNode learnComplexSpecs(){
		refinementTreeRoot = createRefinementTreeRoot();
		TreeX<RefinementNode> mostPromisingNode = null;
		if(refinementTreeRoot == expand(refinementTreeRoot, null)){
			logger.error("Learner can not learn any Specs! Stop here.");
			refinementTreeRoot.print();
			return null;
		}
		refinementTreeRoot.print();
		mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
		while(mostPromisingNode.equals(null) &&
			  (mostPromisingNode.getValue().fitness) < MAX_FITNESS_THRESHOLD &&
			   refinementTreeRoot.size() <= MAX_TREE_SIZE)
		{
			mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
			logger.info("Most Promising Node: " + mostPromisingNode.getValue());
//			mostPromisingNode.getValue().configModel.write(System.out,"TTL");
			List<TreeX<RefinementNode>> oldChildren = mostPromisingNode.getchildren();
			if(mostPromisingNode == expand(mostPromisingNode, oldChildren)){
				logger.error("Learner can not learn any more Specs! Stop here.");
				break;
			}
			refinementTreeRoot.print();
		}

		RefinementNode bestSolution = getMostPromisingNode(refinementTreeRoot, 0).getValue();
		bestSolution.configModel = setIOFiles(bestSolution.configModel, "inputFile.ttl", "outputFile.ttl");
		logger.info("Best solution: " + bestSolution);
//		bestSolution.configModel.write(System.out,"TTL");
		return bestSolution;
	}


	/**
	 * The idea of expanding a node is to generate either simple or complex node
	 * depending on the best fitness.
	 * Simple node generated by creating one child node with the best fitness.
	 * Complex done is a (clone - 2 nodes - merge). 
	 * @param root
	 * @param children
	 * @return the root node after expanding it
	 * @author sherif
	 */
	private TreeX<RefinementNode> expand(TreeX<RefinementNode> root, List<TreeX<RefinementNode>> children) {
		List<RefinementNode> leftRightNodesValues = getLeftRightNodesValues(root.getValue());
		if(leftRightNodesValues.size() == 0){
			logger.warn(root.getValue() + "is not expandable! exit");
			return root;
		}
		if(leftRightNodesValues.size() == 1){
			// generate only one branch
			return new TreeX<RefinementNode>(root, leftRightNodesValues.get(0), children);
		}else{
			Model leftOutputModel = leftRightNodesValues.get(0).outputModels.get(0);
			Model rightOutputModel = leftRightNodesValues.get(1).outputModels.get(0);
			Model mergeModel = leftOutputModel.union(rightOutputModel);
			double mergeFitness = computeFMeasure(mergeModel, targetModel);
			double leftFitness = leftRightNodesValues.get(0).fitness;
			if(mergeFitness > leftFitness){
				// generate only one branch
				return new TreeX<RefinementNode>(root, leftRightNodesValues.get(0), children);
			}else{
				// Generate clone - merge
				TreeX<RefinementNode> cloneNode = createCloneNode(root);
				leftRightNodesValues = getLeftRightNodesValues(cloneNode.getValue());
				//				leftRightNodesValues = getLeftRightNodesValues(leftRightNodesValues, cloneNode.getValue());
//				System.out.println(leftRightNodesValues.get(1).outputDatasets);
				TreeX<RefinementNode> leftNode  = new TreeX<RefinementNode>(cloneNode, leftRightNodesValues.get(0), children);
				TreeX<RefinementNode> rightNode = new TreeX<RefinementNode>(cloneNode, leftRightNodesValues.get(1), children);
				List<TreeX<RefinementNode>> leftRightNodes = new ArrayList<TreeX<RefinementNode>>(Arrays.asList(leftNode, rightNode));
				TreeX<RefinementNode> mergeNode = createMergeNode(leftRightNodes);
				mergeNode.setChildren(children);
				return cloneNode;
			}

		}
	}


	/**
	 * @param leftNodeValue
	 * @param rightNodeValue
	 * @param leftNode
	 * @param rightNode
	 * @param leftRightNodes
	 * @return
	 * @author sherif
	 */
	private TreeX<RefinementNode> createMergeNode(TreeX<RefinementNode> leftNode, TreeX<RefinementNode> rightNode) {
		DeerOperator 	mergeOperator = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
		RefinementNode 	rightNodeValue = rightNode.getValue();
		RefinementNode 	leftNodeValue = leftNode.getValue();
		List<Model> 	mergeInputModels = new ArrayList<Model>(Arrays.asList(leftNodeValue.getOutputModel(), rightNodeValue.getOutputModel()));
		List<Resource>	mergeInputDatasets = new ArrayList<Resource>(Arrays.asList(leftNodeValue.getOutputDataset(), rightNodeValue.getOutputDataset()));
		List<Model> 	mergeOutputModels = mergeOperator.process(mergeInputModels, null);
		List<Model> 	mergeInputConfig = new ArrayList<Model>(Arrays.asList(leftNodeValue.configModel, rightNodeValue.configModel));
		List<Resource> 	mergeOutputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));
		Model 			mergeConfigModel = RDFConfigWriter.addOperator(mergeOperator, null, mergeInputConfig , mergeInputDatasets, mergeOutputDatasets);
		double 			fitness = computeFMeasure(mergeOutputModels.get(0), targetModel);
		RefinementNode mergeNodeValue = new RefinementNode(mergeOperator, fitness, mergeInputModels, mergeOutputModels, mergeConfigModel, mergeInputDatasets, mergeOutputDatasets);
		List<TreeX<RefinementNode>> leftRightNodes = new ArrayList<TreeX<RefinementNode>>(Arrays.asList(leftNode, rightNode));
		TreeX<RefinementNode> mergeNode = new TreeX<RefinementNode>(leftRightNodes ,mergeNodeValue, (TreeX<RefinementNode>) null);
		return mergeNode;
	}

	private TreeX<RefinementNode> createMergeNode(List<TreeX<RefinementNode>> leftrightNodes) {
		DeerOperator 	mergeOperator = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
		RefinementNode 	rightNodeValue = leftrightNodes.get(1).getValue();
		RefinementNode 	leftNodeValue = leftrightNodes.get(0).getValue();
		List<Model> 	mergeInputModels = new ArrayList<Model>(Arrays.asList(leftNodeValue.getOutputModel(), rightNodeValue.getOutputModel()));
		List<Resource>	mergeInputDatasets = new ArrayList<Resource>(Arrays.asList(leftNodeValue.getOutputDataset(), rightNodeValue.getOutputDataset()));
		List<Model> 	mergeOutputModels = mergeOperator.process(mergeInputModels, null);
		List<Model> 	mergeInputConfig = new ArrayList<Model>(Arrays.asList(leftNodeValue.configModel, rightNodeValue.configModel));
		List<Resource> 	mergeOutputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));
		Model 			mergeConfigModel = RDFConfigWriter.addOperator(mergeOperator, null, mergeInputConfig , mergeInputDatasets, mergeOutputDatasets);
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
		Model cloneConfigModel = RDFConfigWriter.addOperator(cloneOperator, null, cloneInputConfig , cloneInputDatasets, cloneOutputDatasets);
		RefinementNode cloneNodeValue = new RefinementNode(cloneOperator, -1, cloneInputModels, cloneOutputModels, cloneConfigModel, cloneInputDatasets, cloneOutputDatasets);
		TreeX<RefinementNode> cloneNode = new TreeX<RefinementNode>(root ,cloneNodeValue, null);
		return cloneNode;
	}


	/**
	 * Learn specification with only modules (enrichment functions)
	 * @return the RefinementNode containing the best found solution of the refinement tree
	 * @author sherif
	 */
	public RefinementNode learnSimpleSpecs(){
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
				configModel = RDFConfigWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
				node = new RefinementNode(module, fitness, root.getValue().getOutputModel(), currentMdl, configModel, inputDataset, outputDataset);
			}
			root.addChild(new TreeX<RefinementNode>(node));
		}
		return root;
	}



	private List<RefinementNode> getLeftRightNodesValues(RefinementNode rootValue) {
		RefinementNode left = null;
		RefinementNode right = null;
		for( DeerModule module : MODULES){
			Model inputModel = rootValue.getOutputModel();
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
					// set dataset and config for the left node 
					Resource inputDataset  = rootValue.outputDatasets.get(0);
					Model configMdl = RDFConfigWriter.addModule(module, parameters, rootValue.configModel, inputDataset, outputDataset);
					RefinementNode node = new RefinementNode(module, fitness, rootValue.getOutputModel(), currentMdl, configMdl, inputDataset, outputDataset);
					if(left == null || left.fitness < fitness){
						right = left;
						left = node;
					}else if(right == null || right.fitness < fitness){
						right = node;
						// set dataset and config for the right node 
						if(rootValue.outputDatasets.size() > 1){
							inputDataset  = rootValue.outputDatasets.get(1);
							right.inputDatasets = new ArrayList<Resource>(Arrays.asList(inputDataset));
							right.configModel = RDFConfigWriter.addModule(module, parameters, rootValue.configModel, inputDataset, outputDataset);
						}

					}
				}
			}
		}
		if(right == null || right.equals(left)){
			return new ArrayList<RefinementNode>(Arrays.asList(left));
		}
		return new ArrayList<RefinementNode>(Arrays.asList(left, right));
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
//		System.out.println("targetModel.difference(currentModel).size() = " + t_c);
//		System.out.println("currentModel.difference(targetModel).size() = " + c_t);
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
		if(root.equals(null)){
			return null;
		}
		// trivial case
		if(root.getchildren() == null || root.getchildren().size() == 0){
			return root;
		}
		// get mostPromesyChild of children
		TreeX<RefinementNode> mostPromesyChild = new TreeX<RefinementNode>(new RefinementNode());
		for(TreeX<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness != -2){ // not a dead node
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
		sourceModel  = Reader.readModel(sourceUri);
		targetModel = Reader.readModel(targetUri);
		long start = System.currentTimeMillis();
		learner.learnComplexSpecs();
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
			ComplexPipeLineLearner.sourceModel  = Reader.readModel(folder + "/input.ttl");
			ComplexPipeLineLearner.targetModel  = Reader.readModel(folder + "/output.ttl");
			long start = System.currentTimeMillis();
			RefinementNode bestSolution = learner.learnSimpleSpecs();
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
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(results);
			//			break;
		}
		System.out.println(results);
	}


	/**
	 * Set the inputFile and outputFile predicates for the first resp. last 
	 * dataset resources. Should used only with auto generated specs files 
	 * where datasets resources 
	 * URIs generated in assending order
	 * @param sConfig
	 * @param inputFile
	 * @param outputFile
	 * @return the input sConfig after adding inputFile and outputFile 
	 * 			to the first and last dataset resources respectively
	 * @author sherif
	 */
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
		return ResourceFactory.createResource(SPECS.uri + "dataset_" + datasetCounter++);
	}

	public static void main(String args[]) throws IOException{
		trivialRun(args);
		//evaluation(args, false, 1);
	}




	// -------------------- old code -----------------------------


	@SuppressWarnings("unused")
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
					Model configModel = RDFConfigWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
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

	@SuppressWarnings("unused")
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
					Model configModel = RDFConfigWriter.addModule(module, parameters, root.getValue().configModel, inputDataset, outputDataset);
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

	/**
	 * Get lift and right nodes without re-evaluating the modules, 
	 * i.e, just fix the left and right node to the input root 
	 * @param leftRightNodesValues
	 * @param root
	 * @return
	 * @author sherif
	 */
	@SuppressWarnings("unused")
	private List<RefinementNode> getLeftRightNodesValues(List<RefinementNode> leftRightNodesValues, RefinementNode root) {
		// input datasets
		Resource leftInputDatasetUri = root.outputDatasets.get(0);
		leftRightNodesValues.get(0).inputDatasets = new ArrayList<Resource>(Arrays.asList(leftInputDatasetUri));
		Resource rightInputDatasetUri = root.outputDatasets.get(1);
		leftRightNodesValues.get(1).inputDatasets = new ArrayList<Resource>(Arrays.asList(rightInputDatasetUri));

		// output datasets
		leftRightNodesValues.get(0).outputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));
		leftRightNodesValues.get(1).outputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI()));

		// config models
		RefinementNode leftValue = leftRightNodesValues.get(0);
		Resource leftModuleUri = RDFConfigAnalyzer.getLastModuleUriOftype(leftValue.module.getType(), leftValue.configModel);
		Resource leftOutputDatasetUri = leftValue.outputDatasets.get(0);
		Model leftConfig = RDFConfigWriter.changeModuleInputOutput(leftValue.configModel, leftModuleUri, leftInputDatasetUri, leftOutputDatasetUri);
		leftValue.configModel = leftConfig.add(root.configModel);

		RefinementNode rightValue = leftRightNodesValues.get(1);
		Resource rightModuleUri = RDFConfigAnalyzer.getLastModuleUriOftype(rightValue.module.getType(), rightValue.configModel);
		Resource rightOutputDatasetUri = rightValue.outputDatasets.get(0);
		Model rightConfig = RDFConfigWriter.changeModuleInputOutput(rightValue.configModel, rightModuleUri, rightInputDatasetUri, rightOutputDatasetUri);
		leftValue.configModel = rightConfig.add(root.configModel);

		return new ArrayList<RefinementNode>(Arrays.asList(leftValue, rightValue));
	}
	
	@SuppressWarnings("unused")
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

}
