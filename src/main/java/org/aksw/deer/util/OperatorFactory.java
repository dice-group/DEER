package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.operator.clone.CloneOperator;
import org.aksw.deer.operator.merge.MergeOperator;
import org.apache.log4j.Logger;


/**
 * @author sherif
 */
@Deprecated
public class OperatorFactory {

  public static final String CLONE_OPERATOR = "clone";
  public static final String MERGE_OPERATOR = "merge";
  public static final String CLONE_OPERATOR_DESCRIPTION =
    "The idea behind the clone operator is to enable parallel execution of different enrichment" +
      " in the same dataset. The clone operator takes one dataset as input and produces n ≥ 2 " +
      "output datasets, which are all identical to the input dataset. Each of the output " +
      "datasets of the clone operator has its own execution (as to be input to any other enrichment" +
      " or operator). Thus, DEER is able to execute all workflows of output datasets in parallel.";
  public static final String MERGE_OPERATOR_DESCRIPTION =
    "The idea behind the merge operator is to enable combining datasets. The merge operator " +
      "takes a set of n ≥ 2 input datasets and merges them into one output dataset containing all" +
      " the input datasets’ triples. As in case of clone operator, the merged output dataset has " +
      "its own execution (as to be input to any other enrichment or operator).";
  private static final Logger logger = Logger.getLogger(OperatorFactory.class.getName());

  /**
   * @return a specific operator instance given its operator's name
   * @author sherif
   */
  public static IOperator createOperator(String name) {
    logger.info("Creating operator with name " + name);

    if (name.equalsIgnoreCase(CLONE_OPERATOR)) {
      return new CloneOperator();
    }
    if (name.equalsIgnoreCase(MERGE_OPERATOR)) {
      return new MergeOperator();
    }
    //TODO Add any new operator here

    logger.error("Sorry, The enrichment " + name + " is not yet implemented. Exit with error ...");
    System.exit(1);
    return null;
  }

  public static String getDescription(String name) {
    String description = "";
    if (name.equalsIgnoreCase(CLONE_OPERATOR)) {
      description = CLONE_OPERATOR_DESCRIPTION;
    } else if (name.equalsIgnoreCase(MERGE_OPERATOR)) {
      description = MERGE_OPERATOR_DESCRIPTION;
    }
    return description;
  }

  /**
   * @return list of names of all implemented operator
   * @author sherif
   */
  public static List<String> getNames() {
    List<String> result = new ArrayList<String>();
    result.add(CLONE_OPERATOR);
    result.add(MERGE_OPERATOR);
    //TODO Add any new enrichment here
    return result;
  }

  /**
   * @return list of instances of all implemented operator
   * @author sherif
   */
  public static List<IOperator> getImplementations() {
    List<IOperator> result = new ArrayList<IOperator>();
    result.add(new CloneOperator());
    result.add(new MergeOperator());
    //TODO Add any new operator here
    return result;
  }
}