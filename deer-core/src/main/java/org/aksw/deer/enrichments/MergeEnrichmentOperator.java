package org.aksw.deer.enrichments;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.List;

@Extension
public class MergeEnrichmentOperator extends AbstractEnrichmentOperator {

  @NotNull
  @Override
  protected List<Model> safeApply(@NotNull List<Model> models) {
    Model merge = ModelFactory.createDefaultModel();
    for (Model model : models) {
      merge.add(model);
    }
    return List.of(merge);
  }

  @NotNull
  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,Integer.MAX_VALUE,1,1);
  }

}