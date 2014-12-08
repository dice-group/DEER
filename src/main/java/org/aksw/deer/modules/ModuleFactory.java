/**
 * 
 */
package org.aksw.deer.modules;

import java.util.ArrayList;
import java.util.List;

import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.apache.log4j.Logger;


/**
 * @author sherif
 *
 */
public class ModuleFactory {
	private static final Logger logger = Logger.getLogger(ModuleFactory.class.getName());

	public static final String DEREFERENCING_MODULE 	= "dereferencing";
	public static final String LINKING_MODULE 		= "linking";
	public static final String NLP_MODULE 			= "nlp";
	public static final String FILTER_MODULE 			= "filter";
	public static final String AUTHORITY_CONFORMATION_MODULE 	= "authorityconformation";
	public static final String PREDICATE_CONFORMATION_MODULE 	= "predicateconformation";
        
	public static final String DEREFERENCING_MODULE_DESCRIPTION =   
			"The purpose of the dereferencing module is to extend the model's Geo-spatial" +
			"information by set of information through specified predicates";
	public static final String LINKING_MODULE_DESCRIPTION       =   
			"The purpose of the linking module is to enrich a model with additional " +
			"geographic information URIs represented in owl:sameAs predicates";
	public static final String NLP_MODULE_DESCRIPTION           =   
			"The purpose of the NLP module is to enrich a model with additional Geo-"+
			"spatial information URIs represented by the addedGeoProperty predicates, "+
			"witch by default is geoknow:relatedTo predicates";
	public static final String AUTHORITY_CONFORMATION_MODULE_DESCRIPTION  =   
			"The purpose of the authority conformation module is to hange a specified source URI " +
			"to a specified target URI, for example using " +
			"source URI of 'http://dbpedia.org' and target URI of 'http://geolift.org' " +
			"changes a resource like 'http://dbpedia.org/Berlin' to 'http://geolift.org/Berlin'";
	public static final String PREDICATE_CONFORMATION_MODULE_DESCRIPTION  =   
			"The purpose of the predicate conformation module is to change a set of source predicates to a set of target predicates." +
			"For example, all rdfs:label can be conformed to become skos:prefLabel";
	public static final String FILTER_MODULE_DESCRIPTION        =   
			"Runs a set of triples patterns against an input model to filter some triples out " +
			"of it and export them to an output model. For example running triple pattern " +
			"'?s <http://dbpedia.org/ontology/abstract> ?o' againt an input model containing " +
			"'http://dbpedia.org/resource/Berlin' will generate output model containing only " +
			"Berlin's abstracts of DBpedia";

	/**
	 * @param name
	 * @return a specific module instance given its module's name
	 * @author sherif
	 */
	public static DeerModule createModule(String name) {
		logger.info("Creating Module with name " + name);

		if(name.equalsIgnoreCase(DEREFERENCING_MODULE))
			return new DereferencingModule();
		if(name.equalsIgnoreCase(LINKING_MODULE ))
			return new LinkingModule();
		if (name.equalsIgnoreCase(NLP_MODULE))
			return new NLPModule();
		if (name.equalsIgnoreCase(AUTHORITY_CONFORMATION_MODULE))
			return new AuthorityConformationModule();
		if (name.equalsIgnoreCase(PREDICATE_CONFORMATION_MODULE))
			return new PredicateConformationModule();
		if (name.equalsIgnoreCase(FILTER_MODULE))
			return new FilterModule();
		//TODO Add any new modules here 
		
		logger.error("Sorry, The module " + name + " is not yet implemented. Exit with error ...");
		System.exit(1);
		return null;
	}

        public static String getDescription(String name) {
            String description = "";

            if(name.equalsIgnoreCase(DEREFERENCING_MODULE)) {
                description = DEREFERENCING_MODULE_DESCRIPTION;
            } else if (name.equalsIgnoreCase(LINKING_MODULE)) {
                description = LINKING_MODULE_DESCRIPTION;
            } else if (name.equalsIgnoreCase(NLP_MODULE)) {
                description = NLP_MODULE_DESCRIPTION;
            } else if (name.equalsIgnoreCase(AUTHORITY_CONFORMATION_MODULE)) {
                description = AUTHORITY_CONFORMATION_MODULE_DESCRIPTION;
            } else if (name.equalsIgnoreCase(PREDICATE_CONFORMATION_MODULE)) {
                description = PREDICATE_CONFORMATION_MODULE_DESCRIPTION;
            } else if (name.equalsIgnoreCase(FILTER_MODULE)) {
                description = FILTER_MODULE_DESCRIPTION;
            }

            return description;
        }
	
	/**
	 * @return list of names of all implemented modules
	 * @author sherif
	 */
	public List<String> getNames(){
		List<String> result = new ArrayList<String>();
		result.add(DEREFERENCING_MODULE);
		result.add(LINKING_MODULE);
		result.add(NLP_MODULE);
		result.add(AUTHORITY_CONFORMATION_MODULE);
		result.add(PREDICATE_CONFORMATION_MODULE);
		result.add(FILTER_MODULE);
		//TODO Add any new modules here 
		return result;
	}
	
	/**
	 * @return list of instances of all implemented modules
	 * @author sherif
	 */
	List<DeerModule> getImplementations(){
		List<DeerModule> result = new ArrayList<DeerModule>();
		result.add(new DereferencingModule());
		result.add(new LinkingModule());
		result.add(new NLPModule());
		result.add(new AuthorityConformationModule());
		result.add(new PredicateConformationModule());
		result.add(new FilterModule());
		//TODO Add any new modules here 
		return result;
	}
}