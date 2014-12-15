/**
 * 
 */
package org.aksw.deer.modules.predicateconformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.json.ParameterType;
import org.aksw.deer.modules.DeerModule;
import org.apache.log4j.Logger;

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

/**
 * @author sherif
 *
 */
public class PredicateConformationModule implements DeerModule{

	private static final Logger logger = Logger.getLogger(PredicateConformationModule.class.getName());
	private Model model = null;

	// parameters list
	private Map<Property, Property> propertyMap = new HashMap<Property, Property>();

	// parameters keys
	private static final String SOURCE_PROPERTY 		= "sourceProperty";
	private static final String SOURCE_PROPERTY_DESC	= "Source property to be replaced by target property";
	private static final String TARGET_PROPERTY 		= "targetProperty";
	private static final String TARGET_PROPERTY_DESC	= "targetProperty to replace source property";

	/**
	 * 
	 *@author sherif
	 */
	public PredicateConformationModule(Model m) {
		super();
		model = m;
	}

	/**
	 * Self configuration
	 * @param source
	 * @param target
	 * @return Map of (key, value) pairs of self configured parameters
	 * @author sherif
	 */
	public Map<String, String> selfConfig(Model source, Model target){
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
		if(commonSubjects.isEmpty()){
			return null;
		}
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
			if(commonObjects.isEmpty()){
				return null;
			}
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
	public PredicateConformationModule() {
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
	public Model process(Model inputModel, Map<String, String> parameters) {
		this.model = inputModel;
		logger.info("--------------- Predicate Conformation Module ---------------");

		//Read parameters
		boolean parameterFound = false;
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
		parameters.add(SOURCE_PROPERTY + "<i>");
		parameters.add(TARGET_PROPERTY + "<i>");
		return parameters;
	}

	@Override
	public List<ParameterType> getParameterWithTypes() {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(new ParameterType(ParameterType.STRING, SOURCE_PROPERTY , SOURCE_PROPERTY_DESC, true));
		parameters.add(new ParameterType(ParameterType.STRING, TARGET_PROPERTY , TARGET_PROPERTY_DESC, true));
		return parameters;
	}

	@Override
	public Resource getType(){
		return SPECS.PredicateConformationModule;
	}
}
