package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.vocabulary.DBpedia;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.deer.vocabulary.SCMSANN;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.fox.binding.FoxApi;
import org.aksw.fox.binding.FoxParameter;
import org.aksw.fox.binding.IFoxApi;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
@Extension
public class NEREnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  public static final Property LITERAL_PROPERTY = DEER.property("literalProperty");

  public static final Property IMPORT_PROPERTY = DEER.property("importProperty");

  public static final Property FOX_URL = DEER.property("foxUrl");

  public static final Property NE_TYPE = DEER.property("neType");

  public static final Property ASK_ENDPOINT = DEER.property("askEndpoint");

  public static final Property DBPEDIA_ENDPOINT_URL = DEER.property("dbpediaEndpointUrl");

  private static final String DEFAULT_FOX_URL = "http://localhost:4444/fox";

  private static final Property DEFAULT_IMPORT_PROPERTY
    = ResourceFactory.createProperty("http://geoknow.org/ontology/relatedTo");

  private static final Logger logger = LoggerFactory.getLogger(NEREnrichmentOperator.class);

  /**
   * Defines the possible (sub)types of named entities to be discovered
   */
  private enum NET {
    ORGANIZATION, LOCATION, PERSON, ALL
  }

  private Property literalProperty;
  private Property importProperty;
  private URL foxUri;
  private String dbpediaEndpointUri;
  private boolean askEndPoint;
  private NET neType;

  @Override
  public @NotNull
  ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(LITERAL_PROPERTY)
      .declareProperty(IMPORT_PROPERTY)
      .declareProperty(FOX_URL)
      .declareProperty(NE_TYPE)
      .declareProperty(ASK_ENDPOINT)
      .declareProperty(DBPEDIA_ENDPOINT_URL)
      .declareValidationShape(getValidationModelFor(NEREnrichmentOperator.class))
      .build();
  }


  private void initializeFields() {
    final ValidatableParameterMap parameters = getParameterMap();
    // mandatory parameter literalProperty
    literalProperty = parameters.getOptional(LITERAL_PROPERTY)
      .map(n -> n.as(Property.class)).orElse(null);
    // optional parameter importProperty
    importProperty = parameters.getOptional(IMPORT_PROPERTY)
      .map(n -> n.as(Property.class)).orElse(DEFAULT_IMPORT_PROPERTY);
    try {
      // optional parameter foxUrl
      String urlString = parameters.getOptional(FOX_URL)
        .map(RDFNode::asResource).map(Resource::getURI).orElse(DEFAULT_FOX_URL);
      foxUri = new URL(urlString);
      // optional parameter dbpediaEndpointUrl
      dbpediaEndpointUri = parameters.getOptional(DBPEDIA_ENDPOINT_URL)
        .map(RDFNode::asResource).map(Resource::getURI).orElse(DBpedia.endPoint);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Encountered bad URL in " + getId() + "!", e);
    }
    // optional parameter askEndpoint
    askEndPoint = parameters.getOptional(ASK_ENDPOINT)
      .map(RDFNode::asLiteral).map(Literal::getBoolean).orElse(false);
    // optional parameter neType
    neType = parameters.getOptional(NE_TYPE)
      .map(RDFNode::asLiteral).map(Literal::getString).map(NET::valueOf).orElse(NET.ALL);
  }

//  /**
//   * Self configuration
//   * Set all parameters to default values, also extract all NEs
//   *
//   * @return Map of (key, value) pairs of self configured parameters
//   */
//  @Override
//  public @NotNull ParameterMap selfConfig(Model source, Model target) {
//    return ParameterMap.EMPTY_INSTANCE;
//  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    initializeFields();
    final Model model = models.get(0);
    final Model resultModel = ModelFactory.createDefaultModel();
    resultModel.add(model);
    if (literalProperty == null) {
      literalProperty = LiteralPropertyRanker.getTopRankedProperty(model);
    }
    model.listStatements(null, literalProperty, (RDFNode) null)
      .filterKeep(statement -> statement.getObject().isLiteral())
      .forEachRemaining(statement -> {
        Resource subject = statement.getSubject();
        Model namedEntityModel = runFOX(statement.getObject().toString());
        if (!namedEntityModel.isEmpty()) {
          switch (neType) {
            case ALL:
              resultModel.add(getNE(namedEntityModel, subject));
              break;
            case LOCATION:
              resultModel.add(getNE(namedEntityModel, subject, SCMSANN.LOCATION));
              break;
            case PERSON:
              resultModel.add(getNE(namedEntityModel, subject, SCMSANN.PERSON));
              break;
            case ORGANIZATION:
              resultModel.add(getNE(namedEntityModel, subject, SCMSANN.ORGANIZATION));
              break;
          }
        }
      });
    return Lists.newArrayList(resultModel);
  }

  /**
   * @return model of places contained in the input model
   */
  private List<Statement> getNE(Model namedEntityModel, Resource subject, Resource type) {
    String sparqlQueryString = "CONSTRUCT {?s ?p ?o} " +
      " WHERE {?s a <" + type + ">. ?s ?p ?o} ";
    Model locationsModel = QueryExecutionFactory.create(sparqlQueryString, namedEntityModel)
      .execConstruct();
    return getNE(locationsModel, subject);
  }

  /**
   * As a generalization of GeoLift
   *
   * @return model of all NEs contained in the input model
   */
  private List<Statement> getNE(Model namedEntityModel, Resource subject) {
    return namedEntityModel.listObjectsOfProperty(SCMSANN.MEANS)
      .filterKeep(RDFNode::isResource)
      .mapWith(RDFNode::asResource)
      .filterDrop(r -> askEndPoint && !isPlace(r))
      .mapWith(r -> ResourceFactory.createStatement(subject, importProperty, r))
      .toList();
  }

  /**
   * @return wither is the input URI is a place of not
   */
  private boolean isPlace(RDFNode uri) {
    boolean result;
    if (uri.toString().contains("http://ns.aksw.org/scms/")) {
      return false;
    }
    String queryString = "ask {<" + uri.toString() + "> a <http://dbpedia.org/ontology/Place>}";
    logger.info("Asking DBpedia for: " + queryString);
    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaEndpointUri, query);
    result = qexec.execAsk();
    logger.info("Answer: " + result);
    return result;
  }

  private Model runFOX(String input) {
    Model namedEntityModel = ModelFactory.createDefaultModel();
    final IFoxApi fox = new FoxApi()
      .setApiURL(foxUri)
      .setTask(FoxParameter.TASK.NER)
      .setOutputFormat(FoxParameter.OUTPUT.TURTLE)
      .setLang(FoxParameter.LANG.EN)
      .setInput(input)
//        .setLightVersion(FoxParameter.FOXLIGHT.)
      .send();
    namedEntityModel.read(new StringReader(fox.responseAsFile().trim()), null, "TTL");
    return namedEntityModel;
  }

  private static class LiteralPropertyRanker {

    static SortedMap<Double, Property> rank(Model model) {
      SortedMap<Double, Property> propertyRanks = new TreeMap<>(Collections.reverseOrder());
      model.listStatements()
        .mapWith(Statement::getPredicate)
        .forEachRemaining((Property property) -> {
          AtomicLong totalLitSize = new AtomicLong(1);
          AtomicLong totalLitCount = new AtomicLong(0);
          model.listObjectsOfProperty(property)
            .filterKeep(RDFNode::isLiteral)
            .mapWith(RDFNode::asLiteral)
            .forEachRemaining(l -> {
              totalLitCount.getAndIncrement();
              totalLitSize.addAndGet(l.toString().length());
            });
          double avgLitSize = (double) totalLitSize.get() / (double) totalLitCount.get();
          propertyRanks.put(avgLitSize, property);
        });
      return propertyRanks;
    }

    static Property getTopRankedProperty(Model model) {
      SortedMap<Double, Property> ranks = rank(model);
      return ranks.get(ranks.firstKey());
    }
  }

}
