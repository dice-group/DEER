/**
 * 
 */
package org.aksw.deer.workflow.specslearner.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.deer.helper.datastructure.FMeasure;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecuter;
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.aksw.deer.workflow.specslearner.ComplexPipeLineLearner;
import org.aksw.deer.workflow.specslearner.RefinementNode;
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
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class ComplexPipeLineLearnerEvaluation {
	private static final Logger logger = Logger.getLogger(ComplexPipeLineLearnerEvaluation.class.getName());
	static String resultStr =
			"mSpecsMdl"  + "\t" +
					"mSpecsOpr"  + "\t" +

			"lSpecsMdl" + "\t" +
			"lSpecsOpr" + "\t" +

			"P_0" + "\t" +
			"R_0" + "\t" +
			"F_0" + "\t" +

			"P" + "\t" +
			"R" + "\t" +
			"F" + "\t" +

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

	public void test(String inputKBFile, int maxSpecSize, double maxSpecComplexity) throws IOException {
		Model s = Reader.readModel(inputKBFile);

		for(float c = 0; c <= maxSpecComplexity ; c++){
			for(int i = 0 ; i <= maxSpecSize ; i++){
				// manual specs
				Model mSpecs = RandomSpecsGenerator.generateSpecs(inputKBFile, i, c);
				resultStr += RDFConfigAnalyzer.getModules(mSpecs).size()  + "\t" ;
				resultStr += RDFConfigAnalyzer.getOperators(mSpecs).size() + "\t";

				// manual enrichment size
				Model t = RDFConfigExecuter.simpleExecute(mSpecs);
				FMeasure f =  FMeasure.computePRF(s, t);
				resultStr += f.P  + "\t" + f.R + "\t" + f.F + "\t";

				// learned specs
				ComplexPipeLineLearner l = new ComplexPipeLineLearner(s, t);
				RefinementNode r = l.learnComplexSpecs();
				resultStr += RDFConfigAnalyzer.getModules(r.configModel).size()  + "\t" ;
				resultStr += RDFConfigAnalyzer.getOperators(r.configModel).size() + "\t";
				f = FMeasure.computePRF(s, r.outputModels.get(0));
				resultStr += f.P  + "\t" + f.R + "\t" + f.F + "\t";

				resultStr += "\n";
			}
		}

	}
	
	
	public String testExampleCount(
			String kbFile, 
			String kbSampleFile, 
			int specsSize,
			double specsComplexity,
			String authority, 
			int examplesCount, 
			double penaltyWeight) throws IOException{
		
		String folder = kbFile.substring(0, kbFile.lastIndexOf("/")+1);
		Model kb = Reader.readModel(kbFile);
		Model mSpecs = RandomSpecsGenerator.generateSpecs(kbFile, specsSize, specsComplexity);
		//		List<Resource> resources = getNResources(authority, kb, exampleCount);
		List<Resource> resources = SimplePipelineLearnerEvaluation.getAllResources(authority, kb);
		Model cbd;
		String cbdOutputFile = new String(), cbdMSpecsOutputFile = new String(), kbMOutputFile = new String();
		Model manuallyEnrichedCBD = ModelFactory.createDefaultModel();
		int foundExamples = 0;
		do{
			// (1) Generate CBDs and save it
			cbd = generateCBDsModel(kb, examplesCount, resources);
			cbdOutputFile = folder + "cbd" + examplesCount + ".ttl";
			Writer.writeModel(cbd, "TTL", cbdOutputFile);

			// (2) Generate a manual-config file to the generated CBD in(1) and save it
			changeFirstDatasetInputFile(mSpecs, cbdOutputFile);
			cbdMSpecsOutputFile = folder + "cbd_" + examplesCount + "_m.ttl";
			kbMOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			mSpecs = SimplePipelineLearnerEvaluation.changeOutputDataset(mSpecs, kbMOutputFile , cbdMSpecsOutputFile);
			mSpecs.setNsPrefixes(mSpecs);
			String cbdManualConfigOutputFile =  folder + "m_config" + examplesCount +".ttl";
			Writer.writeModel(mSpecs, "TTL", cbdManualConfigOutputFile);

			// (3) run the config generated in(2) and save result
			manuallyEnrichedCBD = RDFConfigExecuter.simpleExecute(mSpecs);
			if(!manuallyEnrichedCBD.isIsomorphicWith(cbd)){
				foundExamples++;
			}
		}while(foundExamples < examplesCount);

		// (4) Generate self-config and save it
		ComplexPipeLineLearner learner = new ComplexPipeLineLearner(cbd, manuallyEnrichedCBD, penaltyWeight);
		long start = System.currentTimeMillis();
		RefinementNode bestSolution = learner.learnComplexSpecs();
		if(bestSolution.configModel.equals(null)){
			logger.error("NO Specs learned");
		}
		long learningTime = System.currentTimeMillis() - start;
		Model selfConfEnrichedCBD = bestSolution.getOutputModel();
		String selfConfEnrichedCbdOutputFile =  folder + "cbd" + examplesCount + "s.ttl";
		Writer.writeModel(selfConfEnrichedCBD, "TTL",  selfConfEnrichedCbdOutputFile);
		Model cbdSelfConfig = bestSolution.configModel;
		String cbdSelfConfigOutputFile =  folder + "s_config" + examplesCount + ".ttl";
		Writer.writeModel(cbdSelfConfig, "TTL", cbdSelfConfigOutputFile);

		// (5) Compare manual and self-config in the entire KB
		// I. Generate KBManualConfig and save it
		Model KBManualConfig = ModelFactory.createDefaultModel();
		if(kbSampleFile.isEmpty()){
			KBManualConfig = SimplePipelineLearnerEvaluation.changeInputDataset(mSpecs, cbdOutputFile, kbFile);
		}else{
			KBManualConfig = SimplePipelineLearnerEvaluation.changeInputDataset(mSpecs, cbdOutputFile, kbSampleFile);
		}
		KBManualConfig = SimplePipelineLearnerEvaluation.changeOutputDataset(KBManualConfig, cbdMSpecsOutputFile , kbMOutputFile);
		KBManualConfig.setNsPrefixes(mSpecs);
		String KBManualConfigOutputFile =  folder + "kb_m_config" + examplesCount + ".ttl";
		Writer.writeModel(KBManualConfig, "TTL", KBManualConfigOutputFile);

		// II. Generate manuallyEnrichedKB by applying KBManualConfig to the entire KB and save it
		start = System.currentTimeMillis();
		Model manuallyEnrichedKB = RDFConfigExecuter.simpleExecute(KBManualConfig);
		long manualConfigKBTime = System.currentTimeMillis() - start;

		// III. Generate KBSelfConfig and save it
		String inputFile = "inputFile.ttl";
		Model KBSelfConfig = ModelFactory.createDefaultModel();
		if(kbSampleFile.isEmpty()){
			KBSelfConfig = SimplePipelineLearnerEvaluation.changeInputDataset(cbdSelfConfig, inputFile, kbFile);
		}else{
			KBSelfConfig = SimplePipelineLearnerEvaluation.changeInputDataset(cbdSelfConfig, inputFile, kbSampleFile);
		}
		String outputFile = "outputFile.ttl";
		String kbSOutputFile = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_self_enrichmed.ttl";
		KBSelfConfig = SimplePipelineLearnerEvaluation.changeOutputDataset(KBSelfConfig, outputFile , kbSOutputFile);
		KBSelfConfig.setNsPrefixes(mSpecs);
		String KBSelfConfigOutputFile =  folder + "kb_s_config" + examplesCount + ".ttl";
		Writer.writeModel(KBSelfConfig, "TTL", KBSelfConfigOutputFile);

		// IV. Generate selfConfigEnrichedKB by applying the self config to the entire KgeB 
		start = System.currentTimeMillis();
		Model selfConfigEnrichedKB = RDFConfigExecuter.simpleExecute(KBSelfConfig);
		long selfConfigKBTime = System.currentTimeMillis() - start;
		String selfConfigEnrichedKBoutputFile =  folder + "kb" + examplesCount + "s.ttl";
		Writer.writeModel(selfConfigEnrichedKB, "TTL", selfConfigEnrichedKBoutputFile);

		// V. compare manuallyEnrichedKB vs selfConfigEnrichedKB
		FMeasure fMeasure = FMeasure.computePRF(selfConfigEnrichedKB, manuallyEnrichedKB);

		// add results
		resultStr += examplesCount + "\t";
		resultStr += penaltyWeight + "\t";
		resultStr += RDFConfigAnalyzer.getModules(mSpecs).size() + "\t";
		resultStr += (manualConfigKBTime/ (double)(1000*60)) + "\t";
		resultStr +=  RDFConfigAnalyzer.getModules(KBSelfConfig).size() + "\t";
		resultStr += (selfConfigKBTime/ (double)(1000*60)) + "\t";
		resultStr += (learningTime/ (double)(1000*60)) + "\t";
		resultStr += learner.refinementTreeRoot.size() + "\t";
		resultStr += learner.iterationNr + "\t";
		resultStr += fMeasure.P + "\t";
		resultStr += fMeasure.R+ "\t";
		resultStr += fMeasure.F + "\n";

		System.out.println("**********************************");
		System.out.println(resultStr);
		System.out.println("**********************************");		
		return resultStr;
	}


	/**
	 * @param configMdl
	 * @param inputDatasetFile
	 * @author sherif
	 */
	private void changeFirstDatasetInputFile(Model configMdl,	String inputDatasetFile) {
		Resource firstDataset = ResourceFactory.createResource(SPECS.uri + "dataset_1");
		Resource inputDatasetLocation = ResourceFactory.createResource(inputDatasetFile);
		RDFConfigWriter.changeInputDatasetUri(configMdl, firstDataset, null, inputDatasetLocation);
	}


	/**
	 * @param kb
	 * @param n
	 * @param resources
	 * @return a Model containing CBDs of n resources
	 * @author sherif
	 */
	private Model generateCBDsModel(Model kb, int n, List<Resource> resources) {
		Model cbd;
		cbd = ModelFactory.createDefaultModel();
		for(int j = 0 ; j < n ; j++ ){
			Resource r = resources.iterator().next(); 
			resources.remove(r);
			cbd.add(SimplePipelineLearnerEvaluation.getCBD(r, kb));
		}
		return cbd;
	}

	

	
	
	Resource gerRandomResource(Model kbMdl){
		Resource r = null;
		long offset = (long) (Math.random() * (kbMdl.size() -1));
		String sparqlQueryString= "SELECT ?s {?s ?p ?o} LIMIT 1 OFFSET " + offset;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, kbMdl);
		ResultSet queryResults = qexec.execSelect();
		if(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			r = qs.getResource("?s");
		}
		qexec.close() ;
		return r;
	}
	
	

	public static void main(String args[]) throws IOException{
		String folder = "/home/sherif/JavaProjects/GeoKnow/GeoLift/evaluations/pipeline_learner/dbpedia_AdministrativeRegion/test/";
		String kbFile = folder +"1000_resources_cbds.ttl";
		String kbSampleFile = folder +"100_resources_cbd.ttl";
		for(int i = 4 ; i <= 4 ; i++){
			ComplexPipeLineLearnerEvaluation e = new ComplexPipeLineLearnerEvaluation();
			String manualConfigFile = folder + "m" + i +".ttl";
			String authority = "http://dbpedia.org/resource/Berlin";
			resultStr += "-----------------------------------------------------------\n" + 
					e.testExampleCount(kbFile, kbSampleFile, 2, 0.5, authority, 2, 0.75);
		}
		System.out.println("resultStr");
		File file = new File(folder + "result.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(resultStr);
		bw.close();
	}

}
