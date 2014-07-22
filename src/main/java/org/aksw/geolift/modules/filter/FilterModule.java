/**
 * 
 */
package org.aksw.geolift.modules.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class FilterModule implements GeoLiftModule{

	private static final Logger logger = Logger.getLogger(FilterModule.class.getName());
	private Model model = null;

	// parameters list
	private String 	triplesPattern = "SELECT * WHERE { ?s ?p ?o }";

	

	/**
	 * 
	 *@author sherif
	 */
	public FilterModule(Model m) {
		super();
		model = m;
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#process(com.hp.hpl.jena.rdf.model.Model, java.util.Map)
	 */
	@Override
	public Model process(Model model, Map<String, String> parameters) {
		logger.info("--------------- Filter model ---------------");
		if( parameters.containsKey("triplesPattern")){
			triplesPattern = parameters.get("triplesPattern");
		}
		Model filteredModel = filterModel();
		return filteredModel;
	}
	
	public Model filterModel(){
		Model resultModel = ModelFactory.createDefaultModel();
		String sparqlQueryString = 	"CONSTRUCT {?s ?p ?o} " +
									"WHERE {" + triplesPattern +"} " ;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, model);
		resultModel = qexec.execConstruct();

		return resultModel;
	}


	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	@Override
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("triplesPattern");
		return parameters;
	}

}
