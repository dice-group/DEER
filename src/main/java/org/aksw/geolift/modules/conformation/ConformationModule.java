/**
 * 
 */
package org.aksw.geolift.modules.conformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class ConformationModule implements GeoLiftModule{

	private static final Logger logger = Logger.getLogger(ConformationModule.class.getName());
	private Model model = null;

	// parameters list
	private String 	sourceURI = "";
	private String 	targetURI = "";

	

	/**
	 * 
	 *@author sherif
	 */
	public ConformationModule(Model m) {
		super();
		model = m;
	}

	/**
	 * Self configuration
	 *@author sherif
	 */
	public ConformationModule(Model source, Model target) {
		sourceURI = getMostRedundantUri(source);
		targetURI = getMostRedundantUri(target);
		System.out.println("Source URI: " + sourceURI);
		System.out.println("Target URI: " + targetURI);
	}



	/**
	 * 
	 *@author sherif
	 */
	public ConformationModule() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param m
	 * @return Most redundant source URI in the input model
	 * @author sherif
	 */
	private String getMostRedundantUri(Model m) {
		Multiset<Resource> subjectsMultiset = HashMultiset.create();
		ResIterator listSubjects = m.listSubjects();
		while(listSubjects.hasNext()){
			Resource subject = listSubjects.next();
			subjectsMultiset.add(subject);
		}
		Resource result = ResourceFactory.createResource();
		Integer max = new Integer(0);
		for(Resource r : subjectsMultiset){
			Integer value = subjectsMultiset.count(r);
			if( value > max ){
				max = value;
				result = r;
			}
		}
		return result.toString();
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
		this.model = model;
		logger.info("--------------- Conformation Module ---------------");
		if( parameters.containsKey("sourceURI")){
			sourceURI = parameters.get("sourceURI");
		}
		if( parameters.containsKey("targetURI")){
			targetURI = parameters.get("targetURI");
		}
		Model resultModel = ModelFactory.createDefaultModel();
		StmtIterator statmentsIter = model.listStatements();
		while (statmentsIter.hasNext()) {
			Statement statment = statmentsIter.nextStatement();
			Resource s = statment.getSubject();
			Property p = statment.getPredicate();
			RDFNode  o = statment.getObject();
			if(s.isResource() && s.toString().startsWith(sourceURI)){
				Resource newSubject = ResourceFactory.createResource(s.toString().replaceFirst(sourceURI, targetURI));
				resultModel.add( newSubject , p, o);
			}else{
				resultModel.add( s , p, o);
			}
		}
		return resultModel;
	}


	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	@Override
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("sourceURI");
		parameters.add("targetURI");
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("sourceURI");
		parameters.add("targetURI");
		return parameters;
	}

}
