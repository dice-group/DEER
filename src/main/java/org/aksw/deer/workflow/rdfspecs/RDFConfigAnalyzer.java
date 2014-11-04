/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.vacabularies.SPECS;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigAnalyzer {
	
	public static Set<Resource> getModules(Model configModel){
		Set<Resource> result = new HashSet<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?m {?m <" + RDF.type + "> < " + SPECS.Module + "> . }";
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

}
