/**
 * 
 */
package org.aksw.geolift.modules;

import java.util.ArrayList;
import java.util.List;

import org.aksw.geolift.modules.Dereferencing.URIDereferencing;
import org.aksw.geolift.modules.linking.Linking;
import org.aksw.geolift.modules.nlp.NlpGeoEnricher;
import org.apache.log4j.Logger;


/**
 * @author sherif
 *
 */
public class ModuleFactory {
	private static final Logger logger = Logger.getLogger(GeoLiftModule.class.getName());

	public static final String DEREFERENCING_MODULE 	= "dereferencing";
	public static final String LINKING_MODULE 		= "linking";
	public static final String NLP_MODULE 			= "nlp";
        
        public static final String DEREFERENCING_MODULE_DESCRIPTION =   "The purpose of the dereferencing module is to extend the model’s Geo-spatial" +
                                                                        "information by set of information through speciﬁed predicates";
        public static final String LINKING_MODULE_DESCRIPTION       =   "The purpose of the linking module is to enrich a model with additional " +
                                                                        "geographic information URIs resented in owl:sameAs predicates";
        public static final String NLP_MODULE_DESCRIPTION           =   "The purpose of the NLP module is to enrich a model with additional Geo-"+
                                                                        "spatial information URIs represented by the addedGeoProperty predicates, "+
                                                                        "witch by default is geoknow:relatedTo predicates";

	/**
	 * @param name
	 * @return a specific module instance given its module's name
	 * @author sherif
	 */
	public static GeoLiftModule getModule(String name) {
		logger.info("Getting Module with name "+name);

		if(name.equalsIgnoreCase(DEREFERENCING_MODULE))
			return new URIDereferencing();
		if(name.equalsIgnoreCase(LINKING_MODULE ))
			return new Linking();
		if (name.equalsIgnoreCase(NLP_MODULE))
			return new NlpGeoEnricher();
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
            } else if (name.equalsIgnoreCase(NLP_MODULE_DESCRIPTION)) {
                description = NLP_MODULE_DESCRIPTION;
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
		//TODO Add any new modules here 
		return result;
	}
	
	public List<GeoLiftModule> getImplementations(){
		List<GeoLiftModule> result = new ArrayList<GeoLiftModule>();
		result.add(new URIDereferencing());
		result.add(new Linking());
		result.add(new NlpGeoEnricher());
		//TODO Add any new modules here 
		return result;
	}
}