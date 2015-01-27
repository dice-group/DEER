/**
 * 
 */
package org.aksw.deer.workflow.specslearner.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.deer.helper.datastructure.FMeasure;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecuter;
import org.aksw.deer.workflow.specslearner.RefinementNodeOld;
import org.aksw.deer.workflow.specslearner.SimplePipeLineLearner;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class SimplePipelineLearnerEvaluation {
	private static final Logger logger = Logger.getLogger(SimplePipeLineLearner.class.getName());

	public Model manualConfig = ModelFactory.createDefaultModel();
	public Model selfConfig = ModelFactory.createDefaultModel();
	private Model cbdManualConfig;
	private Model cbdSelfConfig;

	/**
	 * 
	 *@author sherif
	 */
	public SimplePipelineLearnerEvaluation() {
	}

	static String resultStr = 
			"ExampleCount" + "\t" +
					"penaltyWeight" + "\t" +
					"manualConfigComplexity" + "\t" +
					"kbManualConfigTime" + "\t" +
					"selfConfigComplexity" + "\t" +
					"kbSelfConfigTime" + "\t" +
					"LearningTime" + "\t" +
					"TreeSize" +"\t" +
					"IterationNr" + "\t" +
					"P" + "\t" +
					"R" + "\t" +
					"F\n";
	


	public static RefinementNodeOld run(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];

		RefinementNodeOld bestSolution = new RefinementNodeOld();

		for(int i = 1 ; i <= max; i++){
			SimplePipeLineLearner learner = new SimplePipeLineLearner();
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
		return FMeasure.computePRF(selfConfigKB, manualKB);
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

	public static List<Resource> getAllResourcesWithAuthority(String authority, final Model kb){
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

	public static Model getCBD(Resource r, Model m){
		logger.info("Generating CBD of Resource " + r);
		String sparqlQueryString = 
				"DESCRIBE <" + r + ">";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public String testExampleCount(String kbFile, String kbSampleFile, String manualConfigFile, String authority, int exampleCount, double penaltyWeight) throws IOException{
		String folder = kbFile.substring(0, kbFile.lastIndexOf("/")+1);
		Model kb = Reader.readModel(kbFile);
		Model manualConfig = Reader.readModel(manualConfigFile);
		//		List<Resource> resources = getNResources(authority, kb, exampleCount);
		List<Resource> resources = getAllResourcesWithAuthority(authority, kb);
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
			cbdManualConfig = changeInputFile(manualConfig, kbFile , cbdOutputFile);
			cbdMOutputFile = folder + "cbd" + exampleCount + "m.ttl";
			kbMOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			cbdManualConfig = changeOutputFile(cbdManualConfig, kbMOutputFile , cbdMOutputFile);
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
		SimplePipeLineLearner learner = new SimplePipeLineLearner(cbd,manuallyEnrichedCBD, penaltyWeight);
		long start = System.currentTimeMillis();
		RefinementNodeOld bestSolution = learner.run();
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
		Model KBManualConfig = ModelFactory.createDefaultModel();
		if(!kbSampleFile.isEmpty()){
			KBManualConfig = changeInputFile(cbdManualConfig, cbdOutputFile, kbFile);
		}else{
			KBManualConfig = changeInputFile(cbdManualConfig, cbdOutputFile, kbSampleFile);
		}
		KBManualConfig = changeOutputFile(KBManualConfig, cbdMOutputFile , kbMOutputFile);
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
		Model KBSelfConfig = ModelFactory.createDefaultModel();
		if(!kbSampleFile.isEmpty()){
			KBSelfConfig = changeInputFile(cbdSelfConfig, inputFile, kbFile);
		}else{
			KBSelfConfig = changeInputFile(cbdSelfConfig, inputFile, kbSampleFile);
		}
		String outputFile = "outputFile.ttl";
		String kbSOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_self_enrichmed.ttl";
		KBSelfConfig = changeOutputFile(KBSelfConfig, outputFile , kbSOutputFile);
		KBSelfConfig.setNsPrefixes(manualConfig);
		String KBSelfConfigOutputFile =  folder + "kb_s_config" + exampleCount + ".ttl";
		Writer.writeModel(KBSelfConfig, "TTL", KBSelfConfigOutputFile);

		// IV. Generate selfConfigEnrichedKB by applying the self config to the entire KgeB 
		start = System.currentTimeMillis();
		Model selfConfigEnrichedKB = RDFConfigExecuter.simpleExecute(KBSelfConfig);
		long selfConfigKBTime = System.currentTimeMillis() - start;
		String selfConfigEnrichedKBoutputFile =  folder + "kb" + exampleCount + "s.ttl";
		Writer.writeModel(selfConfigEnrichedKB, "TTL", selfConfigEnrichedKBoutputFile);

		// V. compare manuallyEnrichedKB vs selfConfigEnrichedKB
//		System.out.println("+++++++++++++++++++");
//		System.out.println("selfConfigEnrichedKB:" +selfConfigEnrichedKB.size());
//		System.out.println("manuallyEnrichedKB:" + manuallyEnrichedKB.size());
		FMeasure fMeasure = FMeasure.computePRF(selfConfigEnrichedKB, manuallyEnrichedKB);

		// add results
		//			"ExampleCount" + "\t" +
		resultStr += exampleCount + "\t";
		//		"penaltyWeight" + "\t" +	
		resultStr += penaltyWeight + "\t";
		//			"manualConfigXComplexity" + "\t" +
		resultStr += RDFConfigAnalyzer.getModules(manualConfig).size() + "\t";
		//			"manualConfigTime" + "\t" +
		resultStr += (manualConfigKBTime/ (double)(1000*60)) + "\t";
		//			"selfConfigXComplexity" + "\t" +
		resultStr +=  RDFConfigAnalyzer.getModules(KBSelfConfig).size() + "\t";
		//			"SelfConfigTime" + "\t" +
		resultStr += (selfConfigKBTime/ (double)(1000*60)) + "\t";
		//			"LearningTime" + "\t" +
		resultStr += (learningTime/ (double)(1000*60)) + "\t";
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
		return resultStr;
	}


	public static Model changeInputFile(final Model configModel, String oldLocation, String newLocation){
		Model result = ModelFactory.createDefaultModel();
		result = result.union(configModel);
		StmtIterator list = result.listStatements(null, SPECS.inputFile, oldLocation);
		Model removeModel = ModelFactory.createDefaultModel();
		while(list.hasNext()){
			Statement s = list.next();
			removeModel.add(s);
		}
		result = result.remove(removeModel);
		Model addModel = ModelFactory.createDefaultModel();
		list = removeModel.listStatements();
		while(list.hasNext()){
			Statement s = list.next();
			addModel.add(s.getSubject(), s.getPredicate(), newLocation);
		}
		result.add(addModel);
		return result;
	}

	public static Model changeOutputFile(final Model configModel, String oldLocation, String newLocation){
		Model result = ModelFactory.createDefaultModel();
		result = result.union(configModel);
		StmtIterator list = result.listStatements(null, SPECS.outputFile, oldLocation);
		Model removeModel = ModelFactory.createDefaultModel();
		while(list.hasNext()){
			Statement s = list.next();
			removeModel.add(s);
		}
		result = result.remove(removeModel);
		Model addModel = ModelFactory.createDefaultModel();
		list = removeModel.listStatements();
		while(list.hasNext()){
			Statement s = list.next();
			addModel.add(s.getSubject(), s.getPredicate(), newLocation);
		}
		result.add(addModel);
		return result;
	}


	public static void main(String args[]) throws IOException{
		String folder = "/home/sherif/JavaProjects/GeoKnow/GeoLift/evaluations/pipeline_learner/dbpedia_AdministrativeRegion/";
		String kbFile = folder +"1000_resources_cbds.ttl";
		String kbSampleFile = folder +"100_resources_cbd.ttl";
		for(int i = 4 ; i <= 4 ; i++){
			SimplePipelineLearnerEvaluation e = new SimplePipelineLearnerEvaluation();
			String manualConfigFile = folder + "m" + i +".ttl";
			String authority = "http://dbpedia.org/resource/Berlin";
			resultStr += "-----------------------------------------------------------\n" + 
					e.testExampleCount(kbFile, kbSampleFile, manualConfigFile, authority, 2, 0.75);
		}
//		System.out.println("resultStr");
		File file = new File(folder + "result.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(resultStr);
		bw.close();
	}

	public Model getKBSample(Model kb, Resource className, int sampleSize){
		logger.info("Generating "+ sampleSize +" CBDs from class " + className);
		String sparqlQueryString = 
				"DESCRIBE ?s {?s a <" + className + ">} limit " + sampleSize;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, kb);
		Model kbSample = qexec.execDescribe();
		qexec.close();
		return kbSample;

	}
	


}

