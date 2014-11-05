/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.datastructure.FMeasure;
import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.Deer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecuter;

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
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author sherif
 *
 */
public class Evaluation {
	public Model manualConfig = ModelFactory.createDefaultModel();
	public Model selfConfig = ModelFactory.createDefaultModel();

	/**
	 * 
	 *@author sherif
	 */
	public Evaluation() {
	}

	static String resultStr = 
			"ModuleComplexity" + "\t" +
					"Time" + "\t" +
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
		double f = 2 * p * r / (p +r);
		return new FMeasure(p, r, f);
	}

	static double computeFMeasure(Model current, Model target){
		double p = computePrecision(current, target);
		double r = computeRecall(current, target);
		return 2 * p * r / (p +r);
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

	public Model getCBD(Resource r, Model m){
		String sparqlQueryString = 
				"DESCRIBE <" + r + ">";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public void test(String kbFile, String manualConfigFile, String authority, int exampleNr) throws IOException{
		String folder = kbFile.substring(0, kbFile.lastIndexOf("/")+1);
		Model kb = Reader.readModel(kbFile);
		Model manualConfig = Reader.readModel(manualConfigFile);
		List<Resource> resources = getNResources(authority, kb, exampleNr);
		int i = 1;
		for (Resource r : resources) {
			// (1) Generate CBD and save it
			Model cbd = getCBD(r, kb);
			String outputFile = folder + "cbd" + i + ".ttl";
			Writer.writeModel(cbd, "TTL", outputFile);

			// (2) Generate a manual-config file to the generated CBD in(1) and save it
			Model cbdManualConfig = changeInputDatasetLocation(manualConfig, kbFile , outputFile);
			outputFile = folder + "cbd" + i + "m.ttl";
			String oldLocation = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			cbdManualConfig = changeOutputDatasetLocation(cbdManualConfig, oldLocation , outputFile);
			cbdManualConfig.setNsPrefix("gl", SPECS.getURI());
			cbdManualConfig.setNsPrefix("RDFS", RDFS.getURI());
			outputFile =  folder + "m_config" + i +".ttl";
			Writer.writeModel(cbdManualConfig, "TTL", outputFile);

			// (3) run the config generated in(2) and save result
			Model manuallyEnrichedCBD = RDFConfigExecuter.execute(cbdManualConfig).iterator().next();
			if(manuallyEnrichedCBD.difference(cbd).size() == 0){
				continue;
			}

			// (4) Generate self-config  and save it
			SpecsLearn learner = new SpecsLearn();
			learner.sourceModel  = cbd;
			learner.targetModel  = manuallyEnrichedCBD;
			long start = System.currentTimeMillis();
			RefinementNode bestSolution = learner.run();
			long end = System.currentTimeMillis();
			long time = end - start;
			Model selfConfEnrichedCBD = bestSolution.outputModel;
			outputFile =  folder + "cbd" + i + "s.ttl";
			Writer.writeModel(selfConfEnrichedCBD, "TTL",  outputFile);
			Model cbdSelfConfig = bestSolution.configModel;
			outputFile =  folder + "s_config" + i + ".ttl";
			Writer.writeModel(cbdSelfConfig, "TTL", outputFile);

			// (5) Compare manual and self-config in the entire KB
			// I. Generate manuallyEnrichedKB by applying the manual config to the entire DB 
			outputFile = folder + "cbd" + i + ".ttl";
			Model manuallyEnrichedKB = changeInputDatasetLocation(cbdManualConfig, kbFile, outputFile);
			outputFile = folder + "cbd" + i + "m.ttl";
			String newLocation = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			manuallyEnrichedKB = changeOutputDatasetLocation(manuallyEnrichedKB, outputFile , newLocation);
			// II. Generate selfConfigEnrichedKB by applying the self config to the entire DB 
			outputFile = folder + "cbd" + i + ".ttl";
			Model selfConfigEnrichedKB = changeInputDatasetLocation(cbdManualConfig, kbFile, outputFile);
			outputFile = folder + "cbd" + i + "m.ttl";
			newLocation = kbFile.substring(0,kbFile.lastIndexOf(".")) + "_manual_enrichmed.ttl";
			selfConfigEnrichedKB = changeOutputDatasetLocation(selfConfigEnrichedKB, outputFile , newLocation);


			i++;
		}
	}


	public static Model changeInputDatasetLocation(Model configModel, String oldLocation, String newLocation){
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

	public static Model changeOutputDatasetLocation(Model configModel, String oldLocation, String newLocation){
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
		e.test(args[0], args[1], args[2], 1);
	}



}

