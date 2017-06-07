package org.aksw.deer.enrichment;

import java.util.Map;
import org.aksw.deer.util.IEnrichmentFunction;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */

public abstract class AEnrichmentFunction implements IEnrichmentFunction {

  protected Model model = null;
  protected Map<String, String> parameters = null;

  @Override
  public Model apply(Model model) {
    this.model = model;
    if (this.parameters == null) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    return process();
  }

  public void init(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  protected abstract Model process();

  @Deprecated
  public Model apply(Model model, Map<String, String> parameters) {
    init(parameters);
    return apply(model);
  }
}