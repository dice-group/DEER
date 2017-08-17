package org.aksw.deer.enrichment.merge;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.ParameterlessEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
public class MergeEnrichmentOperator extends ParameterlessEnrichmentOperator {

  @Override
  protected List<Model> process() {
    List<Model> result = new ArrayList<>();
    Model merge = ModelFactory.createDefaultModel();
    for (Model model : models) {
      merge.add(model);
    }
    result.add(merge);
    return result;
  }

  @Override
  public String getDescription() {
    return "The idea behind the merge operator is to enable combining datasets. The merge operator " +
      "takes a set of n ≥ 2 input datasets and merges them into one output dataset containing all" +
      " the input datasets’ triples. As in case of clone operator, the merged output dataset has " +
      "its own execution (as to be input to any other enrichment or operator).";
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,Integer.MAX_VALUE,1,1);
  }

}