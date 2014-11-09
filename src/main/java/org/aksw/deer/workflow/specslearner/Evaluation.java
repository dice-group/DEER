/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.datastructure.FMeasure;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.Deer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecuter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author sherif
 *
 */
public class Evaluation {
	private static final Logger logger = Logger.getLogger(SpecsLearn.class.getName());

	public Model manualConfig = ModelFactory.createDefaultModel();
	public Model selfConfig = ModelFactory.createDefaultModel();
	private Model cbdManualConfig;
	private Model cbdSelfConfig;

	/**
	 * 
	 *@author sherif
	 */
	public Evaluation() {
	}

	static String resultStr = 
			"ExampleCount" + "\t" +
					"penaltyWeight" + "\t" +
					"manualConfigComplexity" + "\t" +
					"kbManualConfigTime" + "\t" +
					"selfConfigXComplexity" + "\t" +
					"kbSelfConfigTime" + "\t" +
					"LearningTime" + "\t" +
					"TreeSize" +"\t" +
					"IterationNr" + "\t" +
					"P" + "\t" +
					"R" + "\t" +
					"F\n";

	public static RefinementNode run(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];

		RefinementNode bestSolution = new RefinementNode();

		for(int i = 1 ; i <= max; i++){
			SpecsLearn learner = new SpecsLearn();
			if(isBatch){
				folder = folder + i;
			}
			learner.sourceModel  = Reader.readModel(folder + "/input.ttl");
			learner.targetModel  = Reader.readModel(folder + "/output.ttl");
			long start = System.currentTimeMillis();
			bestSolution = learner.run();
			long end = System.currentTimeMillis();
			long time = end - start;
			resultStr += i + "\t" + time + "\t" + 
					learner.refinementTreeRoot.size() + "\t" + 
					learner.iterationNr + "\t" + 
					//					bestSolution.fitness + "\t" +
					learner.computePrecision(bestSolution.outputModel, learner.targetModel) + "\t" + 
					learner.computeRecall(bestSolution.outputModel, learner.targetModel) + "\t" +
					learner.computeFMeasure
					(bestSolution.outputModel, learner.targetModel);
			Writer.writeModel(bestSolution.configModel, "TTL", folder + "/self_config.ttl");
			//			bestSolution.outputModel.write(System.out,"TTL");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(resultStr);
			//			break;
		}
		System.out.println(resultStr);
		return bestSolution;
	}

	//	resultStr += RDFConfigAnalyzer.getModules(selfConfig).size() + "\t"; // 	ModuleComplexity 

	public static FMeasure evaluateSelfConfig(Model manualConfig, Model selfConfig) throws IOException {
		RDFConfigExecuter configExe = new RDFConfigExecuter();
		Model manualKB = configExe.execute(manualConfig).iterator().next();
		Model selfConfigKB = configExe.execute(selfConfig).iterator().next();
		return getFMeasure(selfConfigKB, manualKB);
	}

	static FMeasure getFMeasure(Model current, Model target){
		double p = computePrecision(current, target);
		double r = computeRecall(current, target);
		double f = 2 * p * r / (p + r);
		return new FMeasure(p, r, f);
	}

	static double computeFMeasure(Model current, Model target){
		double p = computePrecision(current, target);
		double r = computeRecall(current, target);
		return 2 * p * r / (p + r);
	}

	static double computePrecision (Model current, Model target){
		return (double) current.intersection(target).size() / (double) current.size();
	}

	static double computeRecall(Model current, Model target){
		return (double) current.intersection(target).size() / (double) target.size();
	}

	public List<Resource> getNResources(String authority, Model kb, int n){
		List<Resource> results = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?s { ?s ?p ?o. FILTER (STRSTARTS(STR(?s), \'" + authority + "\')) } LIMIT " + n;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, kb);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			results.add(qs.getResource("?s"));
		}
		qexec.close() ;
		return results;
	}

	public List<Resource> getAllResources(String authority, Model kb){
		List<Resource> results = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?s { ?s ?p ?o. FILTER (STRSTARTS(STR(?s), \'" + authority + "\')) }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, kb);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			results.add(qs.getResource("?s"));
		}
		qexec.close() ;
		return results;
	}

	public Model getCBD(Resource r, Model m){
		logger.info("Generating CBD of Resource " + r);
		String sparqlQueryString = 
				"DESCRIBE <" + r + ">";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public void testExampleCount(String kbFile, String manualConfigFile, String authority, int exampleCount, double penaltyWeight) throws IOException{
		String folder = kbFile.substring(0, kbFile.lastIndexOf("/")+1);
		Model kb = Reader.readModel(kbFile);
		Model manualConfig = Reader.readModel(manualConfigFile);
		//		List<Resource> resources = getNResources(authority, kb, exampleCount);
		List<Resource> resources = getAllResources(authority, kb);
		Model cbd = ModelFactory.createDefaultModel();
		String cbdOutputFile = new String(), cbdMOutputFile = new String(), kbMOutputFile = new String();
		Model manuallyEnrichedCBD = ModelFactory.createDefaultModel();
		int foundExamples = 0;
		do{
			cbd = ModelFactory.createDefaultModel();
			for(int j = 0 ; j < exampleCount ; j++ ){
				Resource r = resources.iterator().next(); 
				resources.remove(r);
				// (1) Generate CBDs and save it
				cbd.add(getCBD(r, kb));
			}
			cbdOutputFile = folder + "cbd" + exampleCount + ".ttl";
			Writer.writeModel(cbd, "TTL", cbdOutputFile);

			// (2) Generate a manual-config file to the generated CBD in(1) and save it
			cbdManualConfig = changeInputDataset(manualConfig, kbFile , cbdOutputFile);
			cbdMOutputFile = folder + "cbd" + exampleCount + "m.ttl";
			kbMOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			cbdManualConfig = changeOutputDataset(cbdManualConfig, kbMOutputFile , cbdMOutputFile);
			cbdManualConfig.setNsPrefixes(manualConfig);
			String cbdManualConfigOutputFile =  folder + "m_config" + exampleCount +".ttl";
			Writer.writeModel(cbdManualConfig, "TTL", cbdManualConfigOutputFile);

			// (3) run the config generated in(2) and save result
			manuallyEnrichedCBD = RDFConfigExecuter.simpleExecute(cbdManualConfig);
			if(!manuallyEnrichedCBD.isIsomorphicWith(cbd)){
				foundExamples++;
			}
		}while(foundExamples<exampleCount);

		// (4) Generate self-config and save it
		SpecsLearn learner = new SpecsLearn(cbd,manuallyEnrichedCBD, penaltyWeight);
		long start = System.currentTimeMillis();
		RefinementNode bestSolution = learner.run();
		if(bestSolution.configModel.equals(null)){
			logger.error("NO Specs learned");
		}
		long learningTime = System.currentTimeMillis() - start;
		Model selfConfEnrichedCBD = bestSolution.outputModel;
		String selfConfEnrichedCbdOutputFile =  folder + "cbd" + exampleCount + "s.ttl";
		Writer.writeModel(selfConfEnrichedCBD, "TTL",  selfConfEnrichedCbdOutputFile);
		cbdSelfConfig = bestSolution.configModel;
		String cbdSelfConfigOutputFile =  folder + "s_config" + exampleCount + ".ttl";
		Writer.writeModel(cbdSelfConfig, "TTL", cbdSelfConfigOutputFile);

		// (5) Compare manual and self-config in the entire KB
		// I. Generate KBManualConfig and save it
		Model KBManualConfig = changeInputDataset(cbdManualConfig, cbdOutputFile, kbFile);
		KBManualConfig = changeOutputDataset(KBManualConfig, cbdMOutputFile , kbMOutputFile);
		KBManualConfig.setNsPrefixes(manualConfig);
		String KBManualConfigOutputFile =  folder + "kb_m_config" + exampleCount + ".ttl";
		Writer.writeModel(KBManualConfig, "TTL", KBManualConfigOutputFile);

		// II. Generate manuallyEnrichedKB by applying KBManualConfig to the entire KB and save it
		start = System.currentTimeMillis();
		Model manuallyEnrichedKB = RDFConfigExecuter.simpleExecute(KBManualConfig);
		long manualConfigKBTime = System.currentTimeMillis() - start;
		//		outputFile =  folder + "kb" + exampleCount + "m.ttl";
		//		Writer.writeModel(manuallyEnrichedKB, "TTL", outputFile);

		// III. Generate KBSelfConfig and save it
		String inputFile = "inputFile.ttl";
		Model KBSelfConfig = changeInputDataset(cbdSelfConfig, inputFile, kbFile);
		String outputFile = "outputFile.ttl";
		String kbSOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_self_enrichmed.ttl";
		KBSelfConfig = changeOutputDataset(KBSelfConfig, outputFile , kbSOutputFile);
		KBSelfConfig.setNsPrefixes(manualConfig);
		String KBSelfConfigOutputFile =  folder + "kb_s_config" + exampleCount + ".ttl";
		Writer.writeModel(KBSelfConfig, "TTL", KBSelfConfigOutputFile);

		// IV. Generate selfConfigEnrichedKB by applying the self config to the entire KB 
		start = System.currentTimeMillis();
		Model selfConfigEnrichedKB = RDFConfigExecuter.simpleExecute(KBSelfConfig);
		long selfConfigKBTime = System.currentTimeMillis() - start;
		String selfConfigEnrichedKBoutputFile =  folder + "kb" + exampleCount + "s.ttl";
		Writer.writeModel(selfConfigEnrichedKB, "TTL", selfConfigEnrichedKBoutputFile);

		// V. compare manuallyEnrichedKB vs selfConfigEnrichedKB
		System.out.println("+++++++++++++++++++");
		System.out.println("selfConfigEnrichedKB:" +selfConfigEnrichedKB.size());
		System.out.println("manuallyEnrichedKB:" + manuallyEnrichedKB.size());
		FMeasure fMeasure = getFMeasure(selfConfigEnrichedKB, manuallyEnrichedKB);

		// add results
		//			"ExampleCount" + "\t" +
		resultStr += exampleCount + "\t";
		//		"penaltyWeight" + "\t" +	
		resultStr += penaltyWeight + "\t";
		//			"manualConfigXComplexity" + "\t" +
		resultStr += RDFConfigAnalyzer.getModules(manualConfig).size() + "\t";
		//			"manualConfigTime" + "\t" +
		resultStr += manualConfigKBTime+ "\t";
		//			"selfConfigXComplexity" + "\t" +
		resultStr +=  RDFConfigAnalyzer.getModules(KBSelfConfig).size() + "\t";
		//			"SelfConfigTime" + "\t" +
		resultStr += selfConfigKBTime + "\t";
		//			"LearningTime" + "\t" +
		resultStr += learningTime + "\t";
		//			"TreeSize" +"\t" +
		resultStr += learner.refinementTreeRoot.size() + "\t";
		//			"IterationNr" + "\t" +
		resultStr += learner.iterationNr + "\t";
		//			"P" + "\t" +
		resultStr += fMeasure.P + "\t";
		//			"R" + "\t" +
		resultStr += fMeasure.R+ "\t";
		//			"F\n";
		resultStr += fMeasure.F + "\n";

		System.out.println("**********************************");
		System.out.println(resultStr);
		System.out.println("**********************************");			
	}


	public static Model changeInputDataset(Model configModel, String oldLocation, String newLocation){
		Model result = ModelFactory.createDefaultModel();
		result = result.union(configModel);
		StmtIterator list = result.listStatements(null, SPECS.inputFile, oldLocation);
		Model remove = ModelFactory.createDefaultModel();
		while(list.hasNext()){
			Statement s = list.next();
			remove.add(s);
		}
		result = result.remove(remove);
		Model add = ModelFactory.createDefaultModel();
		list = remove.listStatements();
		while(list.hasNext()){
			Statement s = list.next();
			add.add(s.getSubject(), s.getPredicate(), newLocation);
		}
		result.add(add);
		return result;
	}

	public static Model changeOutputDataset(Model configModel, String oldLocation, String newLocation){
		Model result = ModelFactory.createDefaultModel();
		result = result.union(configModel);
		StmtIterator list = result.listStatements(null, SPECS.outputFile, oldLocation);
		Model remove = ModelFactory.createDefaultModel();
		while(list.hasNext()){
			Statement s = list.next();
			remove.add(s);
		}
		result = result.remove(remove);
		Model add = ModelFactory.createDefaultModel();
		list = remove.listStatements();
		while(list.hasNext()){
			Statement s = list.next();
			add.add(s.getSubject(), s.getPredicate(), newLocation);
		}
		result.add(add);
		return result;
	}


	public static void main(String args[]) throws IOException{
		Evaluation e = new Evaluation();
		String folder = "/home/sherif/JavaProjects/GeoKnow/GeoLift/datasets/usecases/music/jamendo-rdf/";
		String kbFile = folder +"jamendo.ttl";
		String manualConfigFile = folder + "config.ttl";
		String authority = "http://dbtune.org/jamendo/artist/";
		e.testExampleCount(kbFile, manualConfigFile, authority, 3, 0.75);

		//		e.testExampleCount(args[0], args[1], args[2], 2, 0.5);

		//		e.testExampleCount(args[0], args[1], args[2], 1, 0.75);
	}



}

