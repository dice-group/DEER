/**
 * 
 */
package org.aksw.deer.operators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * @author sherif
 *
 */
public class ModelOperatorFactory {
	private static final Logger logger = Logger.getLogger(ModelOperatorFactory.class.getName());

	public static final String MERGE_OPERATOR = "merge";
	public static final String SPLIT_OPERATOR = "split";

	public static final String MERGE_OPERATOR_DESCRIPTION = "The purpose of the merge operator is to merge n>2 models to one single model";
	public static final String SPLIT_OPERATOR_DESCRIPTION = "The purpose of the split operator is to split one model into n>2 identical models";

	/**
	 * @param name
	 * @return a specific module instance given its module's name
	 * @author sherif
	 */
	public static DeerOperator createOperator(String name) {
		logger.info("Getting operator with name "+name);

		if(name.equalsIgnoreCase(MERGE_OPERATOR))
			return new MergeOperator();
		if(name.equalsIgnoreCase(SPLIT_OPERATOR ))
			return new SplitOperator();
		//TODO Add any new operator here 

		logger.error("Sorry, The operator " + name + " is not yet implemented. Exit with error ...");
		System.exit(1);
		return null;
	}

	public static String getDescription(String name) {
		String description = "";

		if(name.equalsIgnoreCase(MERGE_OPERATOR)) {
			description = MERGE_OPERATOR_DESCRIPTION;
		} else if (name.equalsIgnoreCase(SPLIT_OPERATOR)) {
			description = SPLIT_OPERATOR_DESCRIPTION;
		} 

		return description;
	}

	/**
	 * @return list of names of all implemented operators
	 * @author sherif
	 */
	public List<String> getNames(){
		List<String> result = new ArrayList<String>();
		result.add(MERGE_OPERATOR);
		result.add(SPLIT_OPERATOR);
		//TODO Add any new operator here 
		return result;
	}

	/**
	 * @return list of instances of all implemented operators 
	 * @author sherif
	 */
	List<DeerOperator> getImplementations(){
		List<DeerOperator> result = new ArrayList<DeerOperator>();
		result.add(new MergeOperator());
		result.add(new SplitOperator());
		//TODO Add any new operator here 
		return result;
	}
}