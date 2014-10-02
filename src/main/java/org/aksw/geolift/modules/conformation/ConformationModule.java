/**
 * 
 */
package org.aksw.geolift.modules.conformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
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
	private Map<Property, Property> propertyMap = new HashMap<Property, Property>();

	// parameters keys
	private static final String SOURCE_SUBJET_AUTHORITY = "sourceSubjectAuthority";
	private static final String TARGET_SUBJET_AUTHORITY = "targetSubjectAuthority";
	private static final String SOURCE_PROPERTY = "sourceProperty";
	private static final String TARGET_PROPERTY = "targetProperty";


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
		parameters.putAll(selfConfigAuthority(source, target));
		parameters.putAll(selfCongfigProperties(source, target));
		logger.info("Self configuration: " + parameters);
		return parameters;
	}

	private Map<String, String> selfConfigAuthority(Model source, Model target) {
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

	private Map<String, String> selfCongfigProperties(Model source, Model target){
		Map<String, String> parameters = new HashMap<String, String>();
		long i = 1;
		// commonSubjects = common subjects of source and target
		Set<Resource> sSubjects = new HashSet<Resource>();
		ResIterator subjects = source.listSubjects();
		while(subjects.hasNext()){
			sSubjects.add(subjects.next());
		}
		Set<Resource> tSubjects = new HashSet<Resource>();
		subjects = source.listSubjects();
		while(subjects.hasNext()){
			tSubjects.add(subjects.next());
		}
		Set<Resource> commonSubjects = Sets.intersection(sSubjects, tSubjects);
		// commonObjects = for each Subject in commonSubjects find common objects of source and target
		for(Resource s : commonSubjects){
			StmtIterator statements = source.listStatements(s, null , (RDFNode) null);
			Set<RDFNode> sObjects = new HashSet<RDFNode>();
			while(statements.hasNext()){
				sObjects.add(statements.next().getObject()); 
			}
			statements = target.listStatements(s, null , (RDFNode) null);
			Set<RDFNode> tObjects = new HashSet<RDFNode>();
			while(statements.hasNext()){
				tObjects.add(statements.next().getObject()); 
			}
			Set<RDFNode> commonObjects = Sets.intersection(sObjects, tObjects);
			// find different predicate to be conformed
			for(RDFNode o : commonObjects){
				Property sProperty = null, tProperty = null;
				statements = source.listStatements(s, null, o);
				while(statements.hasNext()){
					sProperty = statements.next().getPredicate();
				}
				statements = target.listStatements(s, null, o);
				while(statements.hasNext()){
					tProperty = statements.next().getPredicate();
				}
				if(sProperty != null && tProperty != null && 
						!sProperty.equals(tProperty) &&
						!parameters.containsKey(sProperty.toString()) &&
						!parameters.containsValue(tProperty.toString()) ){
					parameters.put(SOURCE_PROPERTY + i, sProperty.toString());
					parameters.put(TARGET_PROPERTY + i, tProperty.toString());
					i++;
				}
			}
		}
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
	public Model process(Model inputModel, Map<String, String> parameters) {
		this.model = inputModel;
		logger.info("--------------- Conformation Module ---------------");

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
		for(long i = 1 ; parameters.containsKey(SOURCE_PROPERTY + i) && parameters.containsKey(TARGET_PROPERTY + i) ; i++){
			Property inputProperty = ResourceFactory.createProperty(parameters.get(SOURCE_PROPERTY + i));
			Property conformProperty = ResourceFactory.createProperty(parameters.get(TARGET_PROPERTY + i));
			propertyMap.put(inputProperty, conformProperty);
			parameterFound = true;
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
			// conform properties
			if(propertyMap.containsKey(p)){
				p = propertyMap.get(p);
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
		parameters.add(SOURCE_PROPERTY + "<i>");
		parameters.add(TARGET_PROPERTY + "<i>");
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
		parameters.add(SOURCE_PROPERTY + "<i>");
		parameters.add(TARGET_PROPERTY + "<i>");
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
