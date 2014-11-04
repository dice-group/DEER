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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author sherif
 *
 */
public class Evaluation {
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
		Model manualKB = configExe.run(manualConfig).iterator().next();
		Model selfConfigKB = configExe.run(selfConfig).iterator().next();
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

	public static void main(String args[]) throws IOException{
		//		RefinementNode bestNode = run(args, false, 1);
		//		Model manualConfig = Reader.readModel(args[0]);
		//		Model selfConfig = Reader.readModel(args[1]);
		//		System.out.println(evaluateSelfConfig(manualConfig, selfConfig));
		Model kb = Reader.readModel(args[0]);
		List<Resource> resources = getNResources("http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB001", kb, 1);
		for (Resource resource : resources) {
			System.out.println(resource);
			System.out.println("-------------- DESCRIBE ----------------");
			getCBD(resource, kb).write(System.out, "TTL");
			System.out.println("--------------- SELECT ---------------");
			readCBD(resource, kb).write(System.out, "TTL");
			System.out.println("------------------------------");
		}
	}

	public static List<Resource> getNResources(String authority, Model m, int n){
		List<Resource> results = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?s { ?s ?p ?o. FILTER (STRSTARTS(STR(?o), \"" + authority +"\")) } LIMIT " + n;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			results.add(qs.getResource("?s"));
		}
		qexec.close() ;
		return results;
	}

	public static Model getCBD(Resource r, Model m){
		String sparqlQueryString = 
				"DESCRIBE <" + r + ">";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		Model cbd = qexec.execDescribe();
		qexec.close();
		return cbd;
	}

	public static Model readCBD(Resource r, Model m) {
		Model result = ModelFactory.createDefaultModel();
		String sparqlQueryString = "CONSTRUCT {<" + r + "> ?p ?o} WHERE { <" + r + "> ?p ?o.}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
		result = qexec.execConstruct();
		qexec.close() ;
		return result;
	}



}

