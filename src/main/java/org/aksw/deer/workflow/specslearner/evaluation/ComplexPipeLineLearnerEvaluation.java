/**
 *
 */
package org.aksw.deer.workflow.specslearner.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.aksw.deer.helper.datastructure.FMeasure;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecutor;
import org.aksw.deer.workflow.specslearner.ComplexPipeLineLearner;
import org.aksw.deer.workflow.specslearner.RefinementNode;
import org.apache.log4j.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 *
 */
public class ComplexPipeLineLearnerEvaluation{
    private static final Logger logger = Logger.getLogger(ComplexPipeLineLearnerEvaluation.class.getName());
    static String resultStr = new String();
    static final String RESULT_HEADER=
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
    public final double TIME_DEV = 60000d; // minutes

    public void test(String inputKBFile, int maxSpecSize, double maxSpecComplexity) throws IOException {
        Model s = Reader.readModel(inputKBFile);

        for(float c = 0; c <= maxSpecComplexity ; c++){
            for(int i = 0 ; i <= maxSpecSize ; i++){
                // manual specs
                Model mSpecs = RandomSpecsGenerator.generateSpecs(inputKBFile, i, c);
                resultStr += RDFConfigAnalyzer.getModules(mSpecs).size()  + "\t" ;
                resultStr += RDFConfigAnalyzer.getOperators(mSpecs).size() + "\t";

                // manual enrichment size
                Model t = RDFConfigExecutor.simpleExecute(mSpecs);
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
            int exCount,
            double penaltyWeight) throws IOException
    {
        String folder = kbInputFile.substring(0, kbInputFile.lastIndexOf("/")+1);
        Model kbMdl = Reader.readModel(kbInputFile);
        String kbManuallyEnrichedOutputFile = kbInputFile.substring(0,kbInputFile.lastIndexOf(".")) + "_manually_enriched.ttl";
        Model manualSpecsMdl = ModelFactory.createDefaultModel();
        //		mSpecs.write(System.out,"TTL");
        //		List<Resource> resources = getNResources(authority, kb, exampleCount);
        List<Resource> resources = SimplePipelineLearnerEvaluation.getAllResourcesWithAuthority(authority, kbMdl);
        Model cbdMdl;
        String cbdOutFile = new String(), cbdMSpecsOutputFile = new String();
        Model manuallyEnrichedCBDMdl = ModelFactory.createDefaultModel();
        do{
            //			System.out.println("----------------------- mSpecs OLD --------------------------");
            //			mSpecs.write(System.out,"TTL");
            manualSpecsMdl = RandomSpecsGenerator.generateSpecs(kbInputFile, kbManuallyEnrichedOutputFile, specsSize, specsComplexity);

            //			System.out.println("----------------------- mSpecs NEW --------------------------");
            //			manualSpecsMdl.write(System.out,"TTL");
            // (1) Generate CBDs for all positive examples and save it
            cbdMdl = generateCBDsModel(kbMdl, exCount, resources);
            cbdOutFile = folder + "cbd_" + exCount + "_examples.ttl";
            Writer.writeModel(cbdMdl, "TTL", cbdOutFile);

            // (2) Generate a manual-config file to the generated CBDs in (1) and save it
            manualSpecsMdl = SimplePipelineLearnerEvaluation.changeInputFile(manualSpecsMdl, null, cbdOutFile);
            //			manualSpecsMdl.write(System.out,"TTL");
            cbdMSpecsOutputFile = folder + "cbd_" + exCount + "_examples_manually_enriched.ttl";

            //			manualSpecsMdl = SimplePipelineLearnerEvaluation.changeOutputFile(mSpecs, kbMOutputFile , cbdMSpecsOutputFile);
            manualSpecsMdl = SimplePipelineLearnerEvaluation.changeOutputFile(manualSpecsMdl, kbManuallyEnrichedOutputFile , cbdMSpecsOutputFile);
            //			manualSpecsMdl.write(System.out,"TTL");
            String cbdManualSpecOutputFile =  folder + "manual_config_" + exCount +"_examples.ttl";
            Writer.writeModel(manualSpecsMdl, "TTL", cbdManualSpecOutputFile);

            // (3) run the config generated in(2) and save result
            //			System.out.println("----------------------- mSpecs --------------------------");
            //			mSpecs.write(System.out,"TTL");
            //			System.out.println("----------------------- CBDs --------------------------");
            //			cbd.write(System.out,"TTL");
            manuallyEnrichedCBDMdl = RDFConfigExecutor.simpleExecute(manualSpecsMdl);
            //			System.out.println("----------------------- manuallyEnrichedCBD --------------------------");
            //			manuallyEnrichedCBD.write(System.out,"TTL");
        }while(manuallyEnrichedCBDMdl.isIsomorphicWith(cbdMdl));

        // (4) Generate self-config and save it
        ComplexPipeLineLearner learner = new ComplexPipeLineLearner(cbdMdl, manuallyEnrichedCBDMdl, penaltyWeight);
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
        Model kbManualSpecMdl = ModelFactory.createDefaultModel();
        if(kbSampleFile.isEmpty()){
            kbManualSpecMdl = SimplePipelineLearnerEvaluation.changeInputFile(manualSpecsMdl, cbdOutFile, kbInputFile);
        }else{
            kbManualSpecMdl = SimplePipelineLearnerEvaluation.changeInputFile(manualSpecsMdl, cbdOutFile, kbSampleFile);
        }
        kbManualSpecMdl = SimplePipelineLearnerEvaluation.changeOutputFile(kbManualSpecMdl, cbdMSpecsOutputFile , kbManuallyEnrichedOutputFile);
        kbManualSpecMdl.setNsPrefixes(manualSpecsMdl);
        //		String KBManualConfigOutputFile =  folder + "kb_m_config" + examplesCount + ".ttl";
        //		Writer.writeModel(KBManualConfig, "TTL", KBManualConfigOutputFile);

        // II. Generate manuallyEnrichedKB by applying KBManualConfig to the entire KB and save it
        start = System.currentTimeMillis();
        Model manuallyEnrichedKB = RDFConfigExecutor.simpleExecute(kbManualSpecMdl);
        long manualConfigKBTime = System.currentTimeMillis() - start;

        // III. Generate KBSelfConfig and save it
        String inputFile = "inputFile.ttl";
        Model kbSelfConfigMdl = ModelFactory.createDefaultModel();
        if(kbSampleFile.isEmpty()){
            kbSelfConfigMdl = SimplePipelineLearnerEvaluation.changeInputFile(lSpecs, inputFile, kbInputFile);
        }else{
            kbSelfConfigMdl = SimplePipelineLearnerEvaluation.changeInputFile(lSpecs, inputFile, kbSampleFile);
        }
        String outFile = "outputFile.ttl";
        String kbSOutFile = kbInputFile.substring(0,kbInputFile.lastIndexOf(".")) + "_self_enrichmed.ttl";
        kbSelfConfigMdl = SimplePipelineLearnerEvaluation.changeOutputFile(kbSelfConfigMdl, outFile , kbSOutFile);
        kbSelfConfigMdl.setNsPrefixes(manualSpecsMdl);
        //		String KBSelfConfigOutputFile =  folder + "kb_s_config" + examplesCount + ".ttl";
        //		Writer.writeModel(KBSelfConfig, "TTL", KBSelfConfigOutputFile);

        // IV. Generate selfConfigEnrichedKB by applying the self config to the entire KB
        start = System.currentTimeMillis();
        Model selfConfigEnrichedKB = RDFConfigExecutor.simpleExecute(kbSelfConfigMdl);
        long selfConfigKBTime = System.currentTimeMillis() - start;
        //		String selfConfigEnrichedKBoutputFile =  folder + "kb" + examplesCount + "s.ttl";
        //		Writer.writeModel(selfConfigEnrichedKB, "TTL", selfConfigEnrichedKBoutputFile);

        // V. compare manuallyEnrichedKB vs selfConfigEnrichedKB
        FMeasure fMeasure = FMeasure.computePRF(selfConfigEnrichedKB, manuallyEnrichedKB);
        //		System.out.println("----------------------- KB --------------------------");
        //		kb.write(System.out,"TTL");
        //		System.out.println("----------------------- manuallyEnrichedKB --------------------------");
        //		manuallyEnrichedKB.write(System.out,"TTL");
        FMeasure f0 = FMeasure.computePRF(kbMdl, manuallyEnrichedKB);

        // add results
        resultStr += RDFConfigAnalyzer.getModules(manualSpecsMdl).size()+ "\t";
        resultStr += RDFConfigAnalyzer.getOperators(manualSpecsMdl).size()+ "\t";
        resultStr += RDFConfigAnalyzer.getModules(lSpecs).size()+ "\t";
        resultStr += RDFConfigAnalyzer.getOperators(lSpecs).size()+ "\t";
        resultStr += f0.P + "\t";
        resultStr += f0.R+ "\t";
        resultStr += f0.F + "\t";
        resultStr += exCount + "\t";
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
        System.out.println(RESULT_HEADER + resultStr);
        System.out.println("**********************************");
        learner.refinementTreeRoot.print();
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
        if(args.length < 6){
            logger.error("Parameters: kbFile kbSampleFile authority specComplexity Min_MaxSpecSize Min_MaxExampleNr" );
            System.exit(1);
        }
        int i = 0;
        String kbFile 			= args[i]; i++;
        String kbSampleFile 	= args[i]; i++;
        String authority 		= args[i]; i++;
        double specComplexity 	= Double.parseDouble(args[i]); i++;
        int minSpecSize			= Integer.parseInt(args[i].substring(0, args[i].lastIndexOf("_")));
        int maxSpecSize 		= Integer.parseInt(args[i].substring(args[i].lastIndexOf("_")+1,args[i].length())); i++;
        int minExampleNr			= Integer.parseInt(args[i].substring(0, args[i].lastIndexOf("_")));
        int maxExampleNr 		= Integer.parseInt(args[i].substring(args[i].lastIndexOf("_")+1,args[i].length())); i++;
        String folder = kbFile.substring(0, kbFile.lastIndexOf("/")+1); i++;
        for(int exampleNr = minExampleNr ; exampleNr <= maxExampleNr ; exampleNr++){
            for(int specsize = minSpecSize ; specsize <= maxSpecSize ; specsize++){
                logger.info("=============================================== Testing for specs size " + specsize +", with complexity " + specComplexity + "==========================================");
                ComplexPipeLineLearnerEvaluation e = new ComplexPipeLineLearnerEvaluation();
                resultStr += e.testExampleCount(kbFile, kbSampleFile, specsize, specComplexity, authority, exampleNr, 0.75);
            }
        }
        System.out.println("resultStr");
        File file = new File(folder + "result.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(RESULT_HEADER + resultStr);
        bw.close();
    }
    //	public static void main(String args[]) throws IOException{
    //		String folder = "/home/sherif/JavaProjects/GeoKnow/DEER/evaluations/complex_pipeline_learner/jamendo/";
    //		//		String kbFile = folder +"1000_resources_cbds.ttl";
    //		String kbFile = folder + "jamendo.ttl"; //"4_resources_cbds.ttl";
    //		String kbSampleFile = ""; //folder +"100_resources_cbd.ttl";
    //		double specComplexity = 0.4;
    //		for(int specsize = 1 ; specsize <= 5 ; specsize++){
    //			logger.info("=============================================== Testing for spec size " + specsize +", with complexity " + specComplexity + "==========================================");
    //			ComplexPipeLineLearnerEvaluation e = new ComplexPipeLineLearnerEvaluation();
    //			//			String manualConfigFile = folder + "m" + i +".ttl";
    //			//			String authority = "http://dbpedia.org/resource/";
    //			String authority = "http://dbtune.org/jamendo/artist/";
    //			resultStr += e.testExampleCount(kbFile, kbSampleFile, specsize, specComplexity, authority, 2, 0.75);
    //		}
    //		//		System.out.println("resultStr");
    //		File file = new File(folder + "result.txt");
    //		if (!file.exists()) {
    //			file.createNewFile();
    //		}
    //		FileWriter fw = new FileWriter(file.getAbsoluteFile());
    //		BufferedWriter bw = new BufferedWriter(fw);
    //		bw.write(RESULT_HEADER + resultStr);
    //		bw.close();
    //	}

}
