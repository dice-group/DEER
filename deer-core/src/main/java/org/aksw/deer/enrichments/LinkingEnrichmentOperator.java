package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.faraday_cage.parameter.conversions.StringParameterConversion;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.mapping.AMapping;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.List;

/**
 * An {@code EnrichmentOperator} to enrich models with links discovered using LIMES.
 * <p>
 * The {@code LinkingEnrichmentOperator} ...
 */
@Extension
public class LinkingEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(LinkingEnrichmentOperator.class);

  private static final Parameter SPEC_FILE = new ParameterImpl("specFile");

  private static final Parameter LINKS_PART = new ParameterImpl("linksPart",
    StringParameterConversion.getInstance(), true);

  private enum DATASET_PART {
    SOURCE, TARGET
  }

  private String specFile;
  private DATASET_PART linksPart;

  @Override
  public @NotNull ParameterMap createParameterMap() {
    return new ParameterMapImpl(SPEC_FILE, LINKS_PART);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    this.linksPart = DATASET_PART.valueOf(params.getValue(LINKS_PART, "source").toUpperCase());
  }

  @Override
  public @NotNull ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  /**
   * @return model enriched with links generated from a org.aksw.deer.resources.linking tool
   * @param models
   */

  protected List<Model> safeApply(List<Model> models) {
    //@todo: Where does data come from?
    //@todo: implement ability to link internal datasets
    Model model = setPrefixes(models.get(0));
    Configuration cfg = new XMLConfigurationReader(specFile).read();
    addLinksToModel(model, cfg, linksPart);
    return Lists.newArrayList(model);
  }

  /**
   * @return model with prefixes added This function adds prefixes required
   */
  private Model setPrefixes(Model model) {
    String gn = "http://www.geonames.org/ontology#";
    String owl = "http://www.w3.org/2002/07/owl#";

    model.setNsPrefix("gn", gn);
    model.setNsPrefix("owl", owl);
    return model;
  }

  /**
   * @param model:
   *         the model of the dataset to be enriched
   * @param linksPart:
   *         represents the position of the URI to be enriched in the links file
   * @return model enriched with links generated from a org.aksw.deer.resources.linking tool
   */
  private void addLinksToModel(Model model, Configuration cfg, DATASET_PART linksPart) {
    AMapping mapping = Controller.getMapping(cfg).getAcceptanceMapping();
    Property predicate = model.createProperty(cfg.getAcceptanceRelation());
    for (String s : mapping.getMap().keySet()) {
      Resource subject = model.createResource(s);
      for (String t : mapping.getMap().get(s).keySet()) {
        Resource object = model.createResource(t);
        if (linksPart == DATASET_PART.SOURCE) {
          model.add(subject, predicate, object);
        } else {
          model.add(object, predicate, subject);
        }
      }
    }
  }

}
