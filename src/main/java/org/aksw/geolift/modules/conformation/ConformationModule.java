/**
 * 
 */
package org.aksw.geolift.modules.conformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.aksw.geolift.json.ParameterType;

/**
 * @author sherif
 *
 */
public class ConformationModule implements GeoLiftModule{

	private static final Logger logger = Logger.getLogger(ConformationModule.class.getName());
	private Model model = null;

	// parameters list
	private String 	sourceSubjectAuthority = "";
	private String 	targetSubjectAuthority = "";

	

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
	 * Find source/target URI as the most redundant URIs
	 * @param source
	 * @param target
	 * @return Map of (key, value) pairs of self configured parameters
	 * @author sherif
	 */
	public Map<String, String> selfConfig(Model source, Model target) {
		Map<String, String> parameters = new HashMap<String, String>();
		sourceSubjectAuthority = getMostRedundantUri(source);
		targetSubjectAuthority = getMostRedundantUri(target);
		parameters.put("sourceSubjectAuthority", sourceSubjectAuthority);
		parameters.put("targetSubjectAuthority", targetSubjectAuthority);
		logger.info("Self configuration: " + parameters);
		return parameters;
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
			String authority = listSubjects.next().toString();
			if(authority.contains("#")){
				authority = authority.substring(0, authority.lastIndexOf("#"));
			}else{
				authority = authority.substring(0, authority.lastIndexOf("/"));
			}
			subjectsMultiset.add(ResourceFactory.createResource(authority));
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
		if( parameters.containsKey("sourceSubjectAuthority")){
			sourceSubjectAuthority = parameters.get("sourceSubjectAuthority");
		}
		if( parameters.containsKey("targetSubjectAuthority")){
			targetSubjectAuthority = parameters.get("targetSubjectAuthority");
		}
		Model resultModel = ModelFactory.createDefaultModel();
		StmtIterator statmentsIter = model.listStatements();
		while (statmentsIter.hasNext()) {
			Statement statment = statmentsIter.nextStatement();
			Resource s = statment.getSubject();
			Property p = statment.getPredicate();
			RDFNode  o = statment.getObject();
			if(s.isResource() && s.toString().startsWith(sourceSubjectAuthority)){
				Resource newSubject = ResourceFactory.createResource(s.toString().replaceFirst(sourceSubjectAuthority, targetSubjectAuthority));
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
		parameters.add("sourceSubjectAuthority");
		parameters.add("targetSubjectAuthority");
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("sourceSubjectAuthority");
		parameters.add("targetSubjectAuthority");
		return parameters;
	}

        @Override
        public List<ParameterType> getParameterWithTypes() {
            List<ParameterType> parameters = new ArrayList<ParameterType>();
            parameters.add(new ParameterType(ParameterType.STRING, "sourceURI", "Source URI to be replaced.", true));
            parameters.add(new ParameterType(ParameterType.STRING, "targetURI", "Target URI to replace the sourceURI.", true));

            return parameters;
        }
}
