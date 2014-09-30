/**
 * 
 */
package org.aksw.geolift.operators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author sherif
 *
 */
public class ModelOperatorFactory {
	private static final Logger logger = Logger.getLogger(ModelOperatorFactory.class.getName());

	public static final String MERGE_MODEL = "merge";
	public static final String SPLIT_MODEL = "split";

	/**
	 * @param name
	 * @return a specific module instance given its module's name
	 * @author sherif
	 */
	public static GeoLiftOperator createOperator(String name) {
		logger.info("Getting operator with name "+name);

		if(name.equalsIgnoreCase(MERGE_MODEL))
			return new MergeOperator();
		if(name.equalsIgnoreCase(SPLIT_MODEL ))
			return new SplitOperator();
		//TODO Add any new operator here 
		
		logger.error("Sorry, The operator " + name + " is not yet implemented. Exit with error ...");
		System.exit(1);
		return null;
	}
	
	/**
	 * @return list of names of all implemented operators
	 * @author sherif
	 */
	List<String> getNames(){
		List<String> result = new ArrayList<String>();
		result.add(MERGE_MODEL);
		result.add(SPLIT_MODEL);
		//TODO Add any new operator here 
		return result;
	}
	
	/**
	 * @return list of instances of all implemented operators 
	 * @author sherif
	 */
	List<GeoLiftOperator> getImplementations(){
		List<GeoLiftOperator> result = new ArrayList<GeoLiftOperator>();
		result.add(new MergeOperator());
		result.add(new SplitOperator());
		//TODO Add any new operator here 
		return result;
	}
}