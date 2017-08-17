package org.aksw.deer.enrichment.clone;

import java.util.List;
import org.aksw.deer.enrichment.ParameterlessEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import ro.fortsoft.pf4j.Extension;

/**
 * @author Kevin Dreßler <dressler@informatik.uni-leipzig.de>
 */
@Extension
public class CloneEnrichmentOperator extends ParameterlessEnrichmentOperator {

  @Override
  protected List<Model> process() {
    return models;
  }

  @Override
  public String getDescription() {
    return "The idea behind the clone operator is to enable parallel execution of different enrichment" +
      " in the same dataset. The clone operator takes one dataset as input and produces n ≥ 2 " +
      "output datasets, which are all identical to the input dataset. Each of the output " +
      "datasets of the clone operator has its own execution (as to be input to any other enrichment" +
      " or operator). Thus, DEER is able to execute all workflows of output datasets in parallel.";
  }

}
