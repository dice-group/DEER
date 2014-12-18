/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigAnalyzer {
	
	/**
	 * @param configModel
	 * @return all modules n the input configModel
	 * @author sherif
	 */
	public static Set<Resource> getModules(Model configModel){
		Set<Resource> result = new HashSet<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + SPECS.Module + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource module = qs.getResource("?m");
			result.add(module);
		}
		qexec.close() ;
		return result;
	}
	
	/**
	 * @param configModel
	 * @return all datasets n the input configModel
	 * @author sherif
	 */
	public static Set<Resource> getDatasets(Model configModel){
		Set<Resource> result = new HashSet<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?d <" + RDF.type + "> <" + SPECS.Dataset + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource module = qs.getResource("?d");
			result.add(module);
		}
		qexec.close() ;
		return result;
	}
	
	/**
	 * @param configModel
	 * @return The total number of modules and operators included in the configModel
	 * @author sherif
	 */
	public static long size(Model configModel){
		long count = 0;
		String sparqlQueryString = 
				"SELECT (COUNT(DISTINCT ?m) AS ?c)  {?m <" + RDF.type + "> <" + SPECS.Module + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			count = Long.parseLong(qs.getResource("?c").toString());
		}
		qexec.close() ;
		sparqlQueryString =
				"SELECT (COUNT(DISTINCT ?m) AS ?c)  {?m <" + RDF.type + "> <" + SPECS.Module + "> . }";
		QueryFactory.create(sparqlQueryString);
		qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			count += Long.parseLong(qs.getResource("?c").toString());
		}
		qexec.close() ;
		return count;
	}
	
	public static Resource getLastModuleUriOftype(Resource type, Model configModel){
		List<String> results = new ArrayList<String>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + type.getURI() + "> . }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource module = qs.getResource("?m");
			results.add(module.getURI());
		}
		qexec.close() ;
		Collections.sort(results);
		return ResourceFactory.createResource(results.get(results.size()-1));
	}
	
	public static void main(String args[]){
		System.out.println(getModules(Reader.readModel(args[0])));
		
	}

}
