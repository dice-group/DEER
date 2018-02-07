package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.CloneEnrichmentOperator;
import org.aksw.deer.enrichment.EnrichmentOperator;
import org.aksw.deer.enrichment.MergeEnrichmentOperator;
import org.apache.log4j.Logger;


/**
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
   */
  public static EnrichmentOperator createOperator(String name) {
    logger.info("Creating operator with name " + name);

    if (name.equalsIgnoreCase(CLONE_OPERATOR)) {
      return new CloneEnrichmentOperator();
    }
    if (name.equalsIgnoreCase(MERGE_OPERATOR)) {
      return new MergeEnrichmentOperator();
    }

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
   */
  public static List<String> getNames() {
    List<String> result = new ArrayList<>();
    result.add(CLONE_OPERATOR);
    result.add(MERGE_OPERATOR);
    return result;
  }

  /**
   * @return list of instances of all implemented operator
   */
  public static List<EnrichmentOperator> getImplementations() {
    List<EnrichmentOperator> result = new ArrayList<>();
    result.add(new CloneEnrichmentOperator());
    result.add(new MergeEnrichmentOperator());
    return result;
  }
}