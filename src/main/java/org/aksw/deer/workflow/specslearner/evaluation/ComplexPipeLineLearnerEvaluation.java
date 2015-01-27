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
public class ComplexPipeLineLearnerEvaluation{
	private static final Logger logger = Logger.getLogger(ComplexPipeLineLearnerEvaluation.class.getName());
	static String resultStr =
			"mSpecsMdlNr"  + "\t" +
			"mSpecsOprNr"  + "\t" +

			"lSpecsMdlNr" + "\t" +
			"lSpecsOprNr" + "\t" +

			"P_0" + "\t" +
			"R_0" + "\t" +
			"F_0" + "\t" +

			"ExampleCount" + "\t" +
			"penaltyWeight" + "\t" +
			"kbManualConfigTime" + "\t" +
			"kbSelfConfigTime" + "\t" +
			"LearningTime" + "\t" +
			"TreeSize" +"\t" +
			"IterationNr" + "\t" +
			"P" + "\t" +
			"R" + "\t" +
			"F\n";
	public double TIME_DEV = 60000d; // minutes

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
			String kbInputFile, 
			String kbSampleFile, 
			int specsSize,
			double specsComplexity,
			String authority, 
			int examplesCount, 
			double penaltyWeight) throws IOException
			{
		String folder = kbInputFile.substring(0, kbInputFile.lastIndexOf("/")+1);
		Model kb = Reader.readModel(kbInputFile);
		String kbManuallyEnrichedOutputFile = kbInputFile.substring(0,kbInputFile.lastIndexOf(".")) + "_manually_enriched.ttl";
		Model mSpecs = ModelFactory.createDefaultModel();
		//		mSpecs.write(System.out,"TTL");
		//		List<Resource> resources = getNResources(authority, kb, exampleCount);
		List<Resource> resources = SimplePipelineLearnerEvaluation.getAllResourcesWithAuthority(authority, kb);
		Model cbd;
		String cbdOutputFile = new String(), cbdMSpecsOutputFile = new String();
		Model manuallyEnrichedCBD = ModelFactory.createDefaultModel();
		do{
//			System.out.println("----------------------- mSpecs OLD --------------------------");
//			mSpecs.write(System.out,"TTL");
			mSpecs = RandomSpecsGenerator.generateSpecs(kbInputFile, kbManuallyEnrichedOutputFile, specsSize, specsComplexity);
//			System.out.println("----------------------- mSpecs NEW --------------------------");
//			mSpecs.write(System.out,"TTL");
			// (1) Generate CBDs for all positive examples and save it
			cbd = generateCBDsModel(kb, examplesCount, resources);
			cbdOutputFile = folder + "cbd_" + examplesCount + "_examples.ttl";
//			Writer.writeModel(cbd, "TTL", cbdOutputFile);

			// (2) Generate a manual-config file to the generated CBDs in (1) and save it
			mSpecs = SimplePipelineLearnerEvaluation.changeInputFile(mSpecs, null, cbdOutputFile);
			//			mSpecs.write(System.out,"TTL");
			cbdMSpecsOutputFile = folder + "cbd_" + examplesCount + "_examples_manually_enriched.ttl";

			//			mSpecs = SimplePipelineLearnerEvaluation.changeOutputFile(mSpecs, kbMOutputFile , cbdMSpecsOutputFile);
			mSpecs = SimplePipelineLearnerEvaluation.changeOutputFile(mSpecs, kbManuallyEnrichedOutputFile , cbdMSpecsOutputFile);
			//			mSpecs.write(System.out,"TTL");
//			String cbdManualConfigOutputFile =  folder + "manual_config_" + examplesCount +"_examples.ttl";
//			Writer.writeModel(mSpecs, "TTL", cbdManualConfigOutputFile);

			// (3) run the config generated in(2) and save result
//			System.out.println("----------------------- mSpecs --------------------------");
//			mSpecs.write(System.out,"TTL");
//			System.out.println("----------------------- CBDs --------------------------");
//			cbd.write(System.out,"TTL");
			manuallyEnrichedCBD = RDFConfigExecuter.simpleExecute(mSpecs);
//			System.out.println("----------------------- manuallyEnrichedCBD --------------------------");
//			manuallyEnrichedCBD.write(System.out,"TTL");
		}while(manuallyEnrichedCBD.isIsomorphicWith(cbd));

		// (4) Generate self-config and save it
		ComplexPipeLineLearner learner = new ComplexPipeLineLearner(cbd, manuallyEnrichedCBD, penaltyWeight);
		long start = System.currentTimeMillis();
		RefinementNode bestSolution = learner.learnComplexSpecs();
		if(bestSolution.configModel.equals(null)){
			logger.error("NO Specs learned");
		}
		long learningTime = System.currentTimeMillis() - start;
//		Model selfConfEnrichedCBD = bestSolution.getOutputModel();
//		String selfConfEnrichedCbdOutputFile =  folder + "cbd" + examplesCount + "s.ttl";
//		Writer.writeModel(selfConfEnrichedCBD, "TTL",  selfConfEnrichedCbdOutputFile);
		Model lSpecs = bestSolution.configModel;
//		String cbdSelfConfigOutputFile =  folder + "s_config" + examplesCount + ".ttl";
//		Writer.writeModel(lSpecs, "TTL", cbdSelfConfigOutputFile);

		// (5) Compare manual and self-config in the entire KB
		// I. Generate KBManualConfig and save it
		Model KBManualConfig = ModelFactory.createDefaultModel();
		if(kbSampleFile.isEmpty()){
			KBManualConfig = SimplePipelineLearnerEvaluation.changeInputFile(mSpecs, cbdOutputFile, kbInputFile);
		}else{
			KBManualConfig = SimplePipelineLearnerEvaluation.changeInputFile(mSpecs, cbdOutputFile, kbSampleFile);
		}
		KBManualConfig = SimplePipelineLearnerEvaluation.changeOutputFile(KBManualConfig, cbdMSpecsOutputFile , kbManuallyEnrichedOutputFile);
		KBManualConfig.setNsPrefixes(mSpecs);
//		String KBManualConfigOutputFile =  folder + "kb_m_config" + examplesCount + ".ttl";
//		Writer.writeModel(KBManualConfig, "TTL", KBManualConfigOutputFile);

		// II. Generate manuallyEnrichedKB by applying KBManualConfig to the entire KB and save it
		start = System.currentTimeMillis();
		Model manuallyEnrichedKB = RDFConfigExecuter.simpleExecute(KBManualConfig);
		long manualConfigKBTime = System.currentTimeMillis() - start;

		// III. Generate KBSelfConfig and save it
		String inputFile = "inputFile.ttl";
		Model KBSelfConfig = ModelFactory.createDefaultModel();
		if(kbSampleFile.isEmpty()){
			KBSelfConfig = SimplePipelineLearnerEvaluation.changeInputFile(lSpecs, inputFile, kbInputFile);
		}else{
			KBSelfConfig = SimplePipelineLearnerEvaluation.changeInputFile(lSpecs, inputFile, kbSampleFile);
		}
		String outputFile = "outputFile.ttl";
		String kbSOutputFile = kbInputFile.substring(0,kbInputFile.lastIndexOf(".")) + "_self_enrichmed.ttl";
		KBSelfConfig = SimplePipelineLearnerEvaluation.changeOutputFile(KBSelfConfig, outputFile , kbSOutputFile);
		KBSelfConfig.setNsPrefixes(mSpecs);
//		String KBSelfConfigOutputFile =  folder + "kb_s_config" + examplesCount + ".ttl";
//		Writer.writeModel(KBSelfConfig, "TTL", KBSelfConfigOutputFile);

		// IV. Generate selfConfigEnrichedKB by applying the self config to the entire KB 
		start = System.currentTimeMillis();
		Model selfConfigEnrichedKB = RDFConfigExecuter.simpleExecute(KBSelfConfig);
		long selfConfigKBTime = System.currentTimeMillis() - start;
//		String selfConfigEnrichedKBoutputFile =  folder + "kb" + examplesCount + "s.ttl";
//		Writer.writeModel(selfConfigEnrichedKB, "TTL", selfConfigEnrichedKBoutputFile);

		// V. compare manuallyEnrichedKB vs selfConfigEnrichedKB
		FMeasure fMeasure = FMeasure.computePRF(selfConfigEnrichedKB, manuallyEnrichedKB);
//		System.out.println("----------------------- KB --------------------------");
//		kb.write(System.out,"TTL");
//		System.out.println("----------------------- manuallyEnrichedKB --------------------------");
//		manuallyEnrichedKB.write(System.out,"TTL");
		FMeasure f0 = FMeasure.computePRF(kb, manuallyEnrichedKB);

		// add results
		resultStr += RDFConfigAnalyzer.getModules(mSpecs).size()+ "\t";
		resultStr += RDFConfigAnalyzer.getOperators(mSpecs).size()+ "\t";
		resultStr += RDFConfigAnalyzer.getModules(lSpecs).size()+ "\t";
		resultStr += RDFConfigAnalyzer.getOperators(lSpecs).size()+ "\t";
		resultStr += f0.P + "\t";
		resultStr += f0.R+ "\t";
		resultStr += f0.F + "\t";
		resultStr += examplesCount + "\t";
		resultStr += penaltyWeight + "\t";
		resultStr += (manualConfigKBTime/ TIME_DEV) + "\t";
		resultStr += (selfConfigKBTime/ TIME_DEV) + "\t";
		resultStr += (learningTime/ TIME_DEV) + "\t";
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
	 * @param kb
	 * @param n
	 * @param resources
	 * @return a Model containing CBDs of n resources
	 * @author sherif
	 */
	public Model generateCBDsModel(Model kb, int n, List<Resource> resources) {
		Model cbd;
		cbd = ModelFactory.createDefaultModel();
		Iterator<Resource> iterator = resources.iterator();
		int cbdCount = 0;
		while(iterator.hasNext()){
			Resource r = iterator.next();
			cbd.add(SimplePipelineLearnerEvaluation.getCBD(r, kb));
			cbdCount++;
			if(cbdCount == n){
				return cbd;
			}
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
		String folder = "/home/sherif/JavaProjects/GeoKnow/GeoLift/evaluations/complex_pipeline_learner/test/";
		//		String kbFile = folder +"1000_resources_cbds.ttl";
		String kbFile = folder +"4_resources_cbds.ttl";
		String kbSampleFile = ""; //folder +"100_resources_cbd.ttl";
		for(int i = 1 ; i <= 1 ; i++){
			ComplexPipeLineLearnerEvaluation e = new ComplexPipeLineLearnerEvaluation();
			String manualConfigFile = folder + "m" + i +".ttl";
			String authority = "http://dbpedia.org/resource/";
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
