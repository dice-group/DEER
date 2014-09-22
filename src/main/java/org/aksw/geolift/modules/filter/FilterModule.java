/**
 * 
 */
package org.aksw.geolift.modules.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class FilterModule implements GeoLiftModule{

	private static final Logger logger = Logger.getLogger(FilterModule.class.getName());
	private Model model = ModelFactory.createDefaultModel();

	// parameters list
	private String 	triplesPattern = "?s ?p ?o .";
	

	

	/**
	 * 
	 *@author sherif
	 */
	public FilterModule(Model m) {
		super();
		model = m;
	}

	/**
	 * 
	 *@author sherif
	 */
	public FilterModule() {
		// TODO Auto-generated constructor stub
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
		logger.info("--------------- Filter Module ---------------");
		this.model = this.model.union(model);
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

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("triplesPattern");
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#selfConfig(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
	public Map<String, String> selfConfig(Model source, Model target) {
		Map<String, String> parameters = new HashMap<String, String>();
		triplesPattern += "{";
		Set<Property> ps = new HashSet<Property>();
		StmtIterator listStatements = target.listStatements();
		while(listStatements.hasNext()){
			Statement s = listStatements.next();
			ps.add(s.getPredicate());
			 
		}
		
		long i = 0;
		for(Property p : ps){
			triplesPattern += "{?s <" + p.toString() + ">  ?o" + i + " . }";
			i++;
			triplesPattern += (i < ps.size())? "UNION" : "";
		}
		triplesPattern += "}";
		parameters.put("triplesPattern", triplesPattern);
		return parameters;
	}

}
