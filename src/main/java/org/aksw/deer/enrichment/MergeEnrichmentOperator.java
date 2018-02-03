package org.aksw.deer.enrichment;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.enrichment.ParameterlessEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.pf4j.Extension;

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
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,Integer.MAX_VALUE,1,1);
  }

}