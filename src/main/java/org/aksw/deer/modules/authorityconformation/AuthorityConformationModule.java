/**
 * 
 */
package org.aksw.deer.modules.authorityconformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.json.ParameterType;
import org.aksw.deer.modules.DeerModule;
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

/**
 * @author sherif
 *
 */
public class AuthorityConformationModule implements DeerModule{

	private static final Logger logger = Logger.getLogger(AuthorityConformationModule.class.getName());
	private Model model = null;

	// parameters list
	private String 	sourceSubjectAuthority = "";
	private String 	targetSubjectAuthority = "";

	// parameters keys
	public static final String SOURCE_SUBJET_AUTHORITY 		= "sourceSubjectAuthority";
	public static final String SOURCE_SUBJECT_AUTHORITY_DESC	= "Source subject authority to be replaced by Target subject authority.";
	public static final String TARGET_SUBJET_AUTHORITY 		= "targetSubjectAuthority";
	public static final String TARGET_SUBJECT_AUTHORITY_DESC	= "Target subject authority to replace the source subject authority.";

	/**
	 * 
	 *@author sherif
	 */
	public AuthorityConformationModule(Model m) {
		super();
		model = m;
	}

	/**
	 * 
	 *@author sherif
	 */
	public AuthorityConformationModule() {
		// TODO Auto-generated constructor stub
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
		String s = getMostRedundantUri(source);
		String t = getMostRedundantUri(target);
		if(s != t){
			sourceSubjectAuthority = s;
			targetSubjectAuthority = t;
			parameters.put(SOURCE_SUBJET_AUTHORITY, sourceSubjectAuthority);
			parameters.put(TARGET_SUBJET_AUTHORITY, targetSubjectAuthority);
		}
		return parameters;
	}


	/**
	 * @param m
	 * @return Most redundant source URI in the input model
	 * @author sherif
	 */
	public String getMostRedundantUri(Model m) {
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
	public Model process(Model inputModel, Map<String, String> parameters) {
		this.model = inputModel;
		logger.info("--------------- Authority Conformation Module ---------------");

		//Read parameters
		boolean parameterFound = false;
		if( parameters.containsKey(SOURCE_SUBJET_AUTHORITY) && parameters.containsKey(TARGET_SUBJET_AUTHORITY)){
			String s = parameters.get(SOURCE_SUBJET_AUTHORITY);
			String t = parameters.get(TARGET_SUBJET_AUTHORITY);
			if(!s.equals(t)){
				sourceSubjectAuthority = s;
				targetSubjectAuthority = t;
				parameterFound = true;
			}
		}
		if(!parameterFound){
			return model;
		}

		//Conform Model
		Model conformModel = ModelFactory.createDefaultModel();
		StmtIterator statmentsIter = model.listStatements();
		while (statmentsIter.hasNext()) {
			Statement statment = statmentsIter.nextStatement();
			Resource s = statment.getSubject();
			Property p = statment.getPredicate();
			RDFNode  o = statment.getObject();
			// conform subject authority
			if(sourceSubjectAuthority != "" && s.toString().startsWith(sourceSubjectAuthority)){
				s = ResourceFactory.createResource(s.toString().replaceFirst(sourceSubjectAuthority, targetSubjectAuthority));
			}
			conformModel.add(s , p, o);
		}
		model = conformModel;
		return model;
	}




	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	@Override
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add(SOURCE_SUBJET_AUTHORITY);
		parameters.add(TARGET_SUBJET_AUTHORITY);
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add(SOURCE_SUBJET_AUTHORITY);
		parameters.add(TARGET_SUBJET_AUTHORITY);
		return parameters;
	}

	
	public List<ParameterType> getParameterWithTypes() {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(new ParameterType(ParameterType.STRING, SOURCE_SUBJET_AUTHORITY, SOURCE_SUBJECT_AUTHORITY_DESC, true));
		parameters.add(new ParameterType(ParameterType.STRING, TARGET_SUBJET_AUTHORITY, TARGET_SUBJECT_AUTHORITY_DESC, true));
		return parameters;
	}
	
	@Override
	public Resource getType(){
		return SPECS.AuthorityConformationModule;
	}
}
