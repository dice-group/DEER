package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LSPipeline;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.cache.MemoryCache;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.ls.LinkSpecification;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * An {@code EnrichmentOperator} to enrich models with links discovered using LIMES.
 * <p>
 * The {@code LinkingEnrichmentOperator} ...
 */
@Extension
public class LinkingEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(LinkingEnrichmentOperator.class);

  private static final Parameter SPEC_FILE = new ParameterImpl("specFile", false);

  private static final Parameter SELECT_MODE = new ParameterImpl("selectMode", false);

  private static final Parameter LINKS_PART = new ParameterImpl("linksPart", false);

  private static final Parameter LINK_SPECIFICATION = new ParameterImpl("linkSpecification", false);

  private static final Parameter LINKING_PREDICATE = new ParameterImpl("linkingPredicate", false);

  private static final Parameter THRESHOLD = new ParameterImpl("threshold", false);


  private enum DATASET_PART {
    SOURCE, TARGET;
  }


  private enum SELECT {
    BEST, BEST1TO1, BEST1TON, ALL;
  }

  private String specFile;
  private SELECT selectMode;
  private DATASET_PART linksPart;
  private String linkSpecification;
  private String linkingPredicate;
  private double threshold;

  @Override
  public @NotNull
  ParameterMap createParameterMap() {
    return new ParameterMapImpl(SPEC_FILE, LINKS_PART, SELECT_MODE, LINK_SPECIFICATION, LINKING_PREDICATE, THRESHOLD);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    this.specFile = params.getValue(SPEC_FILE);
    this.linksPart = DATASET_PART.valueOf(params.getValue(LINKS_PART, "source").toUpperCase());
    this.selectMode = SELECT.valueOf(params.getValue(SELECT_MODE, "all").toUpperCase());
    this.linkSpecification = params.getValue(LINK_SPECIFICATION);
    this.linkingPredicate = params.getValue(LINKING_PREDICATE);
    this.threshold = Double.parseDouble(params.getValue(THRESHOLD, "0.9").replaceAll("\\^\\^.*$",""));
  }

  @Override
  public @NotNull
  ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  /**
   * @param models
   * @return model enriched with links generated from LIMES
   */
  protected List<Model> safeApply(List<Model> models) {
    if (models.size() == 1) {
      Model model = setPrefixes(models.get(0));
      Configuration cfg = new XMLConfigurationReader(specFile).read();
      if (getOutDegree() == 1) {
        addLinksToModel(model, getMappingFromConfiguration(cfg));
        return Lists.newArrayList(model);
      } else {
        Model linkModel = ModelFactory.createDefaultModel();
        addLinksToModel(linkModel, getMappingFromConfiguration(cfg));
        return Lists.newArrayList(model, linkModel);
      }
    } else {
      ACache source = modelToCache(models.get(0));
      ACache target = modelToCache(models.get(1));
      AMapping mapping = LSPipeline.execute(source, target, new LinkSpecification(linkSpecification, threshold));
      mapping = applySelectModeToMapping(mapping);
      if (getOutDegree() == 1) {
        addLinksToModel(models.get(0), mapping);
        return new MergeEnrichmentOperator().safeApply(models);
      } else if (getOutDegree() == 2) {
        addLinksToModel(models.get(linksPart == DATASET_PART.SOURCE ? 0 : 1), mapping);
        return models;
      } else {
        Model linkModel = ModelFactory.createDefaultModel();
        addLinksToModel(linkModel, mapping);
        return Lists.newArrayList(models.get(0), models.get(1), linkModel);
      }
    }
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1, 2, 1, 3);
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

  private AMapping getMappingFromConfiguration(Configuration cfg) {
    AMapping mapping = Controller.getMapping(cfg).getAcceptanceMapping();
    return applySelectModeToMapping(mapping);
  }

  private AMapping applySelectModeToMapping(AMapping mapping) {
    AMapping result = mapping;
    switch (selectMode) {
      case BEST:
        HashMap<Double, HashMap<String, TreeSet<String>>> reversedMap = mapping.getReversedMap();
        double best = 0d;
        for (Double sim : reversedMap.keySet()) {
          if (sim > best) {
            Map.Entry<String, TreeSet<String>> entry = reversedMap.get(sim).entrySet().iterator().next();
            result = MappingFactory.createDefaultMapping();
            result.add(entry.getKey(), entry.getValue().first(), sim);
          }
        }
        break;
      case BEST1TO1:
        result = mapping.getBestOneToOneMappings(mapping);
        break;
      case BEST1TON:
        result = mapping.getBestOneToNMapping();
        break;
    }
    writeAnalytics("#discovered links", result.size() + "");
    return result;
  }


  private void addLinksToModel(Model model, AMapping mapping) {
    Property predicate = model.createProperty(linkingPredicate);
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

  private ACache modelToCache(Model m) {
    ACache cache = new MemoryCache();
    m.listStatements()
      .filterDrop(stmt -> stmt.getObject().isAnon())
      .forEachRemaining(stmt -> cache.addTriple(stmt.getSubject().getURI(), stmt.getPredicate().getURI(), stmt.getObject().toString()));
    return cache;
  }

}
