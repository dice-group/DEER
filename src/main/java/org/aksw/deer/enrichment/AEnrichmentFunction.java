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
  public Model apply(Model model, Map<String, String> parameters) {
    this.init(model, parameters);
    return process();
  }

  private void init(Model model, Map<String, String> parameters) {
    this.model = model;
    this.parameters = parameters;
  }

  protected abstract Model process();

}