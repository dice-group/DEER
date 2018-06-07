package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.Execution;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
@Extension
public class DefaultModelReader extends AbstractModelIO {

  private static final Logger logger = LoggerFactory.getLogger(DefaultModelReader.class);

  private static final Parameter FROM_URI = new ParameterImpl("fromUri", false);
  private static final Parameter FROM_GRAPH = new ParameterImpl("fromGraph", false);
  private static final Parameter USE_TRIPLE_PATTERN = new ParameterImpl("useTriplePattern", false);
  private static final Parameter USE_SPARQL_CONSTRUCT = new ParameterImpl("useSparqlConstruct", false);
  private static final Parameter USE_ENDPOINT = new ParameterImpl("useEndpoint", false);

  private String fromUri;
  private String useEndpoint;
  private String fromGraph;
  private String triplePattern;
  private String sparqlQuery;

  @Override
  protected void validateAndAccept(@NotNull ParameterMap parameterMap) {
    fromUri = parameterMap.getValue(FROM_URI);
    fromGraph = parameterMap.getValue(FROM_GRAPH);
    triplePattern = parameterMap.getValue(USE_TRIPLE_PATTERN, "?s ?p ?o");
    useEndpoint = parameterMap.getValue(USE_ENDPOINT);
    sparqlQuery = parameterMap.getValue(USE_SPARQL_CONSTRUCT);
  }

  @Override
  public @NotNull ParameterMap createParameterMap() {
    return new ParameterMapImpl(FROM_URI, FROM_GRAPH, USE_ENDPOINT, USE_SPARQL_CONSTRUCT);
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return Execution.toMultiExecution((Model m) ->
      useEndpoint != null ? readModelFromEndPoint() : readModel(fromUri)).apply(data);
  }

  private Model readModel(String locator) {
    locator = injectWorkingDirectory(locator);
    try {
      Model model = ModelFactory.createDefaultModel();
      final long startTime = System.currentTimeMillis();
      model.read(locator);
      logger.info("Loading {} is done in {}ms.", locator,
        (System.currentTimeMillis() - startTime));
      if (sparqlQuery != null) {
        QueryExecution qExec = QueryExecutionFactory.create(sparqlQuery, model);
        model = qExec.execConstruct();
      }
      return model;
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTPException trying to load model from " +
        locator, e);
    }
  }

  private Model readModelFromEndPoint() {
    //@todo: implement new parameter for content type
    //@todo refactor for better tests
    Model result;
    long startTime = System.currentTimeMillis();
    logger.info("Reading dataset from " + useEndpoint);
    String sparqlQueryString;
    if (sparqlQuery != null || fromGraph != null) {
      if (sparqlQuery != null) {
        sparqlQueryString = sparqlQuery;
      } else {
        sparqlQueryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <"
          + fromGraph + "> { " + triplePattern + " } . }";
      }
      QueryExecution qExec = QueryExecutionFactory.sparqlService(useEndpoint, sparqlQueryString);
      result = qExec.execConstruct();
      qExec.close();
    } else if (fromUri != null) {
      sparqlQueryString = "DESCRIBE <" + fromUri + ">";
      QueryEngineHTTP qExec = new QueryEngineHTTP(useEndpoint, sparqlQueryString);
      qExec.setModelContentType(WebContent.contentTypeJSONLD);
      result = qExec.execDescribe();
      qExec.close();
    } else {
      throw new RuntimeException("Neither " + FROM_URI.toString() + " nor " + FROM_GRAPH.toString() +
        " defined to read dataset from " + useEndpoint + ", exit with error.");
    }
    logger.info("Dataset reading is done in " + (System.currentTimeMillis() - startTime) + "ms, " + result.size() + " triples found.");
    return result;
  }
}
