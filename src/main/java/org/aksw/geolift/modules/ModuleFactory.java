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
	
	/**
	 * @return list of names of all implemented modules
	 * @author sherif
	 */
	List<String> getNames(){
		List<String> result = new ArrayList<String>();
		result.add(DEREFERENCING_MODULE);
		result.add(LINKING_MODULE);
		result.add(NLP_MODULE);
		//TODO Add any new modules here 
		return result;
	}
	
	/**
	 * @return list of instances of all implemented modules
	 * @author sherif
	 */
	List<GeoLiftModule> getImplementations(){
		List<GeoLiftModule> result = new ArrayList<GeoLiftModule>();
		result.add(new URIDereferencing());
		result.add(new Linking());
		result.add(new NlpGeoEnricher());
		//TODO Add any new modules here 
		return result;
	}
}