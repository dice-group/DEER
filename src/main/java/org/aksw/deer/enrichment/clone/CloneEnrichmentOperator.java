package org.aksw.deer.enrichment.clone;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.util.Parameter;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author sherif
 */
@Extension
public class CloneEnrichmentOperator extends AEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(CloneEnrichmentOperator.class.getName());

  @Override
  protected List<Model> process() {
    return models;
  }

  public List<Parameter> getParameters() {
    return Collections.emptyList();
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,1,1,1);
  }

  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    return null;
  }
}
