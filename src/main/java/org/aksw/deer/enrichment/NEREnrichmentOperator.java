package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import org.aksw.deer.parameter.Parameter;
import org.aksw.deer.parameter.ParameterImpl;
import org.aksw.deer.parameter.ParameterMap;
import org.aksw.deer.parameter.ParameterMapImpl;
import org.aksw.deer.vocabulary.DBpedia;
import org.aksw.deer.vocabulary.SCMSANN;
import org.aksw.fox.binding.FoxApi;
import org.aksw.fox.binding.FoxParameter;
import org.aksw.fox.binding.IFoxApi;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

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
public class NEREnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(NEREnrichmentOperator.class.getName());

  private static final Parameter LITERAL_PROPERTY = new ParameterImpl("literalProperty");
  private static final Parameter IMPORT_PROPERTY = new ParameterImpl("importProperty");
  private static final Parameter FOX_URL = new ParameterImpl("foxUrl");
  private static final Parameter NE_TYPE = new ParameterImpl("neType");
  private static final Parameter ASK_ENDPOINT = new ParameterImpl("askEndpoint");
  private static final Parameter DBPEDIA_ENDPOINT_URL = new ParameterImpl("dbpediaEndpointUrl");
  private static final String DEFAULT_FOX_URL = "http://localhost:4444/fox";
  private static final String DEFAULT_IMPORT_PROPERTY = "http://geoknow.org/ontology/relatedTo";

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
  public @NotNull ParameterMap createParameterMap() {
    return new ParameterMapImpl(LITERAL_PROPERTY, IMPORT_PROPERTY, FOX_URL, NE_TYPE,
      ASK_ENDPOINT, DBPEDIA_ENDPOINT_URL);
  }

  @Override
  public void init(@NotNull ParameterMap params) {
    this.literalProperty = params.getValue(LITERAL_PROPERTY) == null ?
      null : ResourceFactory.createProperty(params.getValue(LITERAL_PROPERTY));
    this.importProperty = ResourceFactory.createProperty(params.getValue(IMPORT_PROPERTY, DEFAULT_IMPORT_PROPERTY));
    try {
      this.foxUri = new URL(params.getValue(FOX_URL), DEFAULT_FOX_URL);
      this.dbpediaEndpointUri = params.getValue(DBPEDIA_ENDPOINT_URL, DBpedia.endPoint);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unresolvable bad URL encountered.", e);
    }
    this.askEndPoint = Boolean.valueOf(params.getValue(ASK_ENDPOINT, "false"));
    this.neType = NET.valueOf(params.getValue(NE_TYPE, NET.ALL.toString()));
  }

  /**
   * Self configuration
   * Set all parameters to default values, also extract all NEs
   *
   * @return Map of (key, value) pairs of self configured parameters
   */
  @Override
  public @NotNull ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  @Override
  protected List<Model> process() {
    Model model = models.get(0);
    Model resultModel = ModelFactory.createDefaultModel();
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
    try {
      final IFoxApi fox = new FoxApi()
        .setApiURL(foxUri)
        .setTask(FoxParameter.TASK.NER)
        .setOutputFormat(FoxParameter.OUTPUT.TURTLE)
        .setLang(FoxParameter.LANG.EN)
        .setInput(input)
//        .setLightVersion(FoxParameter.FOXLIGHT.)
        .send();
      namedEntityModel.read(new StringReader(fox.responseAsFile().trim()), null, "TTL");
    } catch (Exception e) {
      logger.error(e);
      logger.error(input);
    }
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
