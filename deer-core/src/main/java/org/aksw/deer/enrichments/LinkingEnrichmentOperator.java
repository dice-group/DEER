package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.DeerAnalyticsStore;
import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.FaradayCageContext;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LSPipeline;
import org.aksw.limes.core.controller.MLPipeline;
import org.aksw.limes.core.evaluation.evaluator.EvaluatorType;
import org.aksw.limes.core.exceptions.UnsupportedMLImplementationException;
import org.aksw.limes.core.io.cache.ACache;
import org.aksw.limes.core.io.cache.MemoryCache;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.ls.LinkSpecification;
import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.mapping.MappingFactory;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * An {@code EnrichmentOperator} to enrich models with links discovered using LIMES.
 * <p>
 * The {@code LinkingEnrichmentOperator} ...
 */
@Extension
public class LinkingEnrichmentOperator extends AbstractParameterizedEnrichmentOperator implements SelfConfigurable, ReverseLearnable {

  private static final Logger logger = LoggerFactory.getLogger(LinkingEnrichmentOperator.class);

  public static final Property SPEC_FILE = DEER.property("specFile");

  public static final Property SELECT_MODE = DEER.property("selectMode");

  public static final Property LINKS_PART = DEER.property("linksPart");

  public static final Property LINK_SPECIFICATION = DEER.property("linkSpecification");

  public static final Property LINKING_PREDICATE = DEER.property("linkingPredicate");

  public static final Property THRESHOLD = DEER.property("threshold");

  public static final Property USE_ML = DEER.property("useML");

  private enum DATASET_PART {
    SOURCE, TARGET;
  }

  private enum SELECT {
    BEST, BEST1TO1, BEST1TON, ALL;
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(SPEC_FILE)
      .declareProperty(LINKS_PART)
      .declareProperty(SELECT_MODE)
      .declareProperty(LINK_SPECIFICATION)
      .declareProperty(LINKING_PREDICATE)
      .declareProperty(THRESHOLD)
      .declareValidationShape(getValidationModelFor(LinkingEnrichmentOperator.class))
      .build();
  }

  /**
   * @param models
   * @return model enriched with links generated from LIMES
   */
  @SuppressWarnings("Duplicates")
  protected List<Model> safeApply(List<Model> models) {
    // retrieve parameters
    ValidatableParameterMap parameters = getParameterMap();
    // spec file must be present if in degree == 1
    final Optional<String> specFile = parameters.getOptional(SPEC_FILE)
      .map(RDFNode::asLiteral).map(Literal::getString);
    // link specification must be present if in degree == 2
    final Optional<String> linkSpecification = parameters.getOptional(LINK_SPECIFICATION)
      .map(RDFNode::asLiteral).map(Literal::getString);
    // threshold is optional, default 0.9
    final double threshold = parameters.getOptional(THRESHOLD)
      .map(RDFNode::asLiteral).map(Literal::getDouble).orElse(.9d);
    final boolean useML = parameters.getOptional(USE_ML)
      .map(RDFNode::asLiteral).map(Literal::getBoolean).orElse(false);
    //links part is optional, default "source"
    final DATASET_PART linksPart = parameters.getOptional(LINKS_PART)
      .map(n -> n.asLiteral().getString().toUpperCase())
      .map(DATASET_PART::valueOf).orElse(DATASET_PART.SOURCE);
    if (getInDegree() == 1 && specFile.isPresent()) {
      Model model = setPrefixes(models.get(0));
      if (model.size() == 0) {
        return List.of(model);
      }
      Configuration cfg = new XMLConfigurationReader(specFile.get()).read();
      final Property linkingPredicate = ResourceFactory.createProperty(cfg.getAcceptanceRelation());
      if (getOutDegree() == 1) {
        addLinksToModel(linksPart, model, getMappingFromConfiguration(cfg), linkingPredicate);
        return Lists.newArrayList(model);
      } else {
        Model linkModel = ModelFactory.createDefaultModel();
        addLinksToModel(linksPart, linkModel, getMappingFromConfiguration(cfg), linkingPredicate);
        return Lists.newArrayList(model, linkModel);
      }
    } else if (getInDegree() == 2) {
      ACache source = modelToCache(models.get(0));
      ACache target = modelToCache(models.get(1));
      // safeguard against empty models
      if (models.get(0).size() == 0 || models.get(1).size() == 0 ) {
        if (getOutDegree() == 1) {
          return List.of(models.get(0).add(models.get(1)));
        } else if (getOutDegree() == 2) {
          return models;
        } else {
          return List.of(models.get(0), models.get(1), ModelFactory.createDefaultModel());
        }
      }
      AMapping mapping;
      if (linkSpecification.isPresent()) {
        mapping = LSPipeline.execute(
          source, target, new LinkSpecification(linkSpecification.get(), threshold)
        );
      } else {
        Configuration cfg = new Configuration();
        cfg.setMlAlgorithmName("Wombat Simple");
        cfg.setMlImplementationType(MLImplementationType.UNSUPERVISED);
        cfg.setMlPseudoFMeasure(EvaluatorType.PF_MEASURE);
        cfg.setMlAlgorithmParameters(List.of());
        //@todo implement option to pass ml parameters
        try {
          mapping = MLPipeline.execute(
            source, target, cfg, cfg.getMlAlgorithmName(), cfg.getMlImplementationType(),
            cfg.getMlAlgorithmParameters(), "", cfg.getMlPseudoFMeasure(), 0);
        } catch (UnsupportedMLImplementationException e) {
          throw new IllegalArgumentException(e);
        }
      }
      mapping = applySelectModeToMapping(mapping);
      if (getOutDegree() == 1) {
        addLinksToModel(linksPart, models.get(0), mapping);
        return new MergeEnrichmentOperator().safeApply(models);
      } else if (getOutDegree() == 2) {
        addLinksToModel(linksPart,
          models.get(linksPart == DATASET_PART.SOURCE ? 0 : 1), mapping);
        return models;
      } else {
        Model linkModel = ModelFactory.createDefaultModel();
        addLinksToModel(linksPart, linkModel, mapping);
        return Lists.newArrayList(models.get(0), models.get(1), linkModel);
      }
    } else if (getInDegree() == 1){
      throw new IllegalStateException("Incoming edges amount to 1 but " + SPEC_FILE +
        " not declared in " + getId() + "!");
    } else {
      throw new IllegalStateException("Incoming edges amount to 2 but neither " + LINK_SPECIFICATION +
        " nor " + USE_ML + " declared in " + getId() + "!");
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
    // parameter selectMode is optional, defaults to "all"
    final SELECT selectMode = getParameterMap().getOptional(SELECT_MODE)
      .map(n -> n.asLiteral().getString().toUpperCase())
      .map(SELECT::valueOf).orElse(SELECT.ALL);
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
    DeerAnalyticsStore.write(FaradayCageContext.getRunId(), getId(),
      new JSONObject().put("newDiscoveredLinks", result.size()));
    return result;
  }

  private void addLinksToModel(DATASET_PART linksPart, Model model, AMapping mapping) {
    // parameter linkingPredicate must always be declared
    final Property linkingPredicate = getParameterMap().get(LINKING_PREDICATE).as(Property.class);
    addLinksToModel(linksPart, model, mapping, linkingPredicate);
  }

  private void addLinksToModel(DATASET_PART linksPart, Model model, AMapping mapping, Property linkingPredicate) {
    // parameter linkingPredicate must always be declared
    for (String s : mapping.getMap().keySet()) {
      Resource subject = model.createResource(s);
      for (String t : mapping.getMap().get(s).keySet()) {
        Resource object = model.createResource(t);
        if (linksPart == DATASET_PART.SOURCE) {
          model.add(subject, linkingPredicate, object);
        } else {
          model.add(object, linkingPredicate, subject);
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

  @Override
  public double predictApplicability(List<Model> inputs, Model target) {
    Model s = inputs.get(0);
    Model t = inputs.get(1);
    List<Statement> sameAs = target
      .listStatements(null, OWL.sameAs, (RDFNode) null).toList();
    return sameAs.size() == 0d ? 0 : 0.5d + (sameAs.stream()
      .filter(stmt -> !s.contains(stmt) && !t.contains(stmt) && (isPotentialLink(s, t, stmt) || isPotentialLink(t, s, stmt)))
      .mapToDouble(stmt -> 1.0d)
      .sum()/(2*sameAs.size()));
  }

  @Override
  public List<Model> reverseApply(List<Model> inputs, Model target) {
    Model sI = inputs.get(0);
    Model tI = inputs.get(1);
    Model s = ModelFactory.createDefaultModel();
    Model t = ModelFactory.createDefaultModel();
    List<Statement> straightLinks = target.listStatements(null, OWL.sameAs, (RDFNode) null)
      .filterKeep(stmt -> !sI.contains(stmt) && !tI.contains(stmt) && isPotentialLink(sI, tI, stmt))
      .toList();
    List<Statement> inverseLinks = target.listStatements(null, OWL.sameAs, (RDFNode) null)
      .filterKeep(stmt -> !sI.contains(stmt) && !tI.contains(stmt) && isPotentialLink(tI, sI, stmt))
      .mapWith(stmt -> target.createStatement(stmt.getResource(), stmt.getPredicate(), stmt.getSubject()))
      .toList();
    final boolean invert = straightLinks.size() < inverseLinks.size();
    (invert ? inverseLinks : straightLinks)
    .forEach(stmt -> {
      addSubgraphOfResourceToModel(stmt.getSubject(), s);
      addSubgraphOfResourceToModel(stmt.getResource(), t);
    });
    return List.of(s,t);
  }

  private boolean isPotentialLink(Model s, Model t, Statement stmt) {
    return stmt.getObject().isResource()
      && s.contains(stmt.getSubject(), null)
      && t.contains(stmt.getResource(), null);
  }

  private void addSubgraphOfResourceToModel(Resource resource, Model model) {
    Deque<RDFNode> stack = new ArrayDeque<>();
    Set<Resource> visited = new HashSet<>();
    stack.push(resource);
    while (!stack.isEmpty()) {
      RDFNode n = stack.pop();
      if (n.isResource() && !visited.contains(n.asResource())) {
        Resource r = n.asResource();
        r.listProperties()
          .filterDrop(stmt -> stmt.getPredicate().equals(OWL.sameAs))
          .forEachRemaining(stmt -> {
          model.add(stmt);
          stack.push(stmt.getObject());
        });
        visited.add(r);
      }
    }
  }

  @Override
  public ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target, ValidatableParameterMap prototype) {
    if (parameterMapSupplier != null) {
      return parameterMapSupplier.get();
    }
    return createParameterMap()
      .add(USE_ML, ResourceFactory.createTypedLiteral(true))
      .add(LINKING_PREDICATE, OWL.sameAs)
      .init();
  }

  public ExecutionNode.DegreeBounds getLearnableDegreeBounds() {
    return new DegreeBounds(2,2,1,1);
  }


  private static Supplier<ValidatableParameterMap> parameterMapSupplier = null;
  public static void setStaticLearning(Supplier<ValidatableParameterMap> x) {
    parameterMapSupplier = x;
  }

}
