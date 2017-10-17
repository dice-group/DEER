package org.aksw.deer.enrichment.dereferencing;

import com.google.common.collect.Lists;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.parameter.DefaultParameter;
import org.aksw.deer.parameter.DefaultParameterMap;
import org.aksw.deer.parameter.Parameter;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

import java.util.List;

/**
 * 
 */
@Extension
public class DereferencingEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(DereferencingEnrichmentOperator.class);

  private static final Parameter SOURCE_SUBJECT_AUTHORITY = new DefaultParameter(
    "sourceSubjectAuthority",
    "Source subject authority to be replaced by Target subject authority");

  public DereferencingEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> process() {
    Model input = models.get(0);
    Model output = ModelFactory.createDefaultModel();

    return Lists.newArrayList(output);
  }


  @Override
  public String getDescription() {
    return "The purpose of the authority conformation enrichment is to change a specified " +
      "source URI to a specified target URI, for example using " +
      "source URI of 'http://dbpedia.org' and target URI of 'http://example.org' " +
      "changes a resource like 'http://dbpedia.org/Berlin' to 'http://example.org/Berlin'";
  }

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap parameters = createParameterMap();
    return parameters;
  }

  @Override
  public ParameterMap createParameterMap() {
    return new DefaultParameterMap(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public void accept(ParameterMap params) {
    this.sourceSubjectAuthority = params.getValue(SOURCE_SUBJECT_AUTHORITY);
    this.targetSubjectAuthority = params.getValue(TARGET_SUBJECT_AUTHORITY);
  }

}