/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.deer.helper.datastructure.Tree;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.modules.GeoLiftModule;
import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;



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
					new AuthorityConformationModule(), 
					new PredicateConformationModule(), 
					new DereferencingModule()
					));
	
	public double penaltyWeight = 0.5;// [0, 1]

	private int datasetCounter = 1;
	public static Model sourceModel = ModelFactory.createDefaultModel();
	public static Model targetModel = ModelFactory.createDefaultModel();
	public Tree<RefinementNode> refinementTreeRoot = new Tree<RefinementNode>(new RefinementNode());
	RDFConfigWriter configWriter = new RDFConfigWriter();
	public int iterationNr = 0;

	private final double 	MAX_FITNESS_THRESHOLD = 1; 
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

	public RefinementNode run(){
		refinementTreeRoot = createRefinementTreeRoot();
		refinementTreeRoot = expandNode(refinementTreeRoot);
		Tree<RefinementNode> mostPromisingNode = getMostPromisingNode(refinementTreeRoot, penaltyWeight);
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
		return bestSolution;
	}
	
	private Tree<RefinementNode> createRefinementTreeRoot(){
		Resource outputDataset  = ResourceFactory.createResource(SPECS.uri + "Dataset_" + datasetCounter++);
		Model config = ModelFactory.createDefaultModel();
		double f = -Double.MAX_VALUE;
		RefinementNode initialNode = new RefinementNode(null,f,sourceModel,sourceModel,outputDataset,outputDataset,config);
		return new Tree<RefinementNode>(null,initialNode, null);
	}
	
	private Tree<RefinementNode> expandNode(Tree<RefinementNode> root) {
		for( GeoLiftModule module : MODULES){
			Model inputModel = root.getValue().outputModel;
			Map<String, String> parameters = module.selfConfig(inputModel, targetModel);
			Resource inputDataset  = root.getValue().outputDataset;
			Model configMdl = ModelFactory.createDefaultModel();
			RefinementNode node = new RefinementNode();
			logger.info(module.getClass().getSimpleName() + "' self-config parameter(s):" + parameters);
			if(parameters == null || parameters.size() == 0){
				// mark as dead end, fitness = -2
				configMdl = root.getValue().configModel;
				node = new RefinementNode( module, -2, sourceModel, sourceModel, inputDataset, inputDataset, configMdl);
			}else{
				Model currentMdl = module.process(inputModel, parameters);
				double fitness;
				if(currentMdl == null || currentMdl.size() == 0 || currentMdl.isIsomorphicWith(inputModel)){
					fitness = -2;
				}else{
//					fitness = computeFitness(currentMdl, targetModel);
					fitness = computeFMeasure(currentMdl, targetModel);
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
		long t_c = targetModel.difference(currentModel).size();
		long c_t = currentModel.difference(targetModel).size();
		System.out.println("targetModel.difference(currentModel).size() = " + t_c);
		System.out.println("currentModel.difference(targetModel).size() = " + c_t);
		return 1- ((double)(t_c + c_t) / (double)(currentModel.size() + targetModel.size()));
	}
	
	double computeFMeasure(Model currentModel, Model targetModel){
		double p = computePrecision(currentModel, targetModel);
		double r = computeRecall(currentModel, targetModel);
		return 2 * p * r / (p +r);
		
	}
	
	double computePrecision (Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) currentModel.size();
	}
	
	double computeRecall(Model currentModel, Model targetModel){
		return (double) currentModel.intersection(targetModel).size() / (double) targetModel.size();
	}
	
	private Tree<RefinementNode> getMostPromisingNode(Tree<RefinementNode> root, double penaltyWeight){
		// trivial case
		if(root.getchildren() == null || root.getchildren().size() == 0){
			return root;
		}
		// get mostPromesyChild of children
		Tree<RefinementNode> mostPromesyChild = new Tree<RefinementNode>(new RefinementNode());
		for(Tree<RefinementNode> child : root.getchildren()){
			if(child.getValue().fitness >= 0){
				Tree<RefinementNode> promesyChild = getMostPromisingNode(child, penaltyWeight);
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
	private double computePenality(Tree<RefinementNode> promesyChild) {
		long childrenCount = promesyChild.size() - 1;
		double childrenPenalty = (CHILDREN_PENALTY_WEIGHT * childrenCount) / refinementTreeRoot.size();
		long level = promesyChild.level();
		double complextyPenalty = (COMPLEXITY_PENALTY_WEIGHT * level) / refinementTreeRoot.depth();
		return  childrenPenalty + complextyPenalty;
	}

	public static void main(String args[]) throws IOException{
//		trivialRun(args);
		evaluation(args, false, 1);
	}
	
	public static void trivialRun(String args[]){
		String sourceUri = args[0];
		String targetUri = args[1];
		SpecsLearn learner = new SpecsLearn();
		learner.sourceModel  = Reader.readModel(sourceUri);
		learner.targetModel = Reader.readModel(targetUri);
		long start = System.currentTimeMillis();
		learner.run();
		long end = System.currentTimeMillis();
		logger.info("Done in " + (end - start) + "ms");
	}
	
	public static void evaluation(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];
		String results = "ModuleCount\tTime\tTreeSize\tIterationNr\tP\tR\tF\n";
		for(int i = 1 ; i <= max; i++){
			SpecsLearn learner = new SpecsLearn();
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
					learner.computePrecision(bestSolution.outputModel, targetModel) + "\t" + 
					learner.computeRecall(bestSolution.outputModel, targetModel) + "\t" +
					learner.computeFMeasure
					(bestSolution.outputModel, targetModel);
			Writer.writeModel(bestSolution.configModel, "TTL", folder + "/self_config.ttl");
//			bestSolution.outputModel.write(System.out,"TTL");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(results);
//			break;
		}
		System.out.println(results);
	}

}
