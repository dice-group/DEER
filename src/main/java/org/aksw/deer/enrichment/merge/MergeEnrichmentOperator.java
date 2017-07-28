package org.aksw.deer.enrichment.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author sherif
 */
@Extension
public class MergeEnrichmentOperator extends AEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(MergeEnrichmentOperator.class.getName());

  @Override
  protected List<Model> process() {
    logger.info("--------------- Merge Operator ---------------");
    List<Model> result = new ArrayList<>();
    Model merge = ModelFactory.createDefaultModel();
    for (Model model : models) {
      merge.add(model);
    }
    result.add(merge);
    return result;
  }

  public List<String> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public List<String> getNecessaryParameters() {
    return new ArrayList<>();
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Resource getType() {
    return SPECS.MergeOperator;
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,Integer.MAX_VALUE,1,1);
  }

  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    return null;
  }
}