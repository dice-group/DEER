package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 *
 *
 *
 */
@Extension
public class SparqlModelReader extends AbstractModelReader {

  private static final Logger logger = LoggerFactory.getLogger(SparqlModelReader.class);

  public static final Property FROM_ENDPOINT = DEER.property("fromEndpoint");

  public static final Property USE_SPARQL_CONSTRUCT = DEER.property("useSparqlConstruct");

  public static final Property USE_SPARQL_DESCRIBE_OF= DEER.property("useSparqlDescribeOf");


  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(FROM_ENDPOINT)
      .declareProperty(USE_SPARQL_CONSTRUCT)
      .declareProperty(USE_SPARQL_DESCRIBE_OF)
      .declareValidationShape(getValidationModelFor(SparqlModelReader.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return ExecutionNode.toMultiExecution((Model m) -> readModelFromEndPoint()).apply(data);
  }

  @SuppressWarnings("Duplicates")
  private Model readModelFromEndPoint() {
    ValidatableParameterMap parameters = getParameterMap();
    String fromEndpoint = parameters.get(FROM_ENDPOINT).asResource().getURI();
    final Optional<String> sparqlQuery = parameters.getOptional(USE_SPARQL_CONSTRUCT)
      .map(RDFNode::asLiteral).map(Literal::getString);
    final Optional<String> describeTarget = parameters.getOptional(USE_SPARQL_DESCRIBE_OF)
      .map(RDFNode::asResource).map(Resource::getURI);
    //@todo: implement new parameter for content type
    //@todo refactor for better tests
    final Model result;
    long startTime = System.currentTimeMillis();
    logger.info("Reading dataset from " + fromEndpoint);
    final String sparqlQueryString;
    if (sparqlQuery.isPresent()) {
      sparqlQueryString = sparqlQuery.get();
      final QueryExecution qExec = QueryExecutionFactory.sparqlService(fromEndpoint, sparqlQueryString);
      result = qExec.execConstruct();
      qExec.close();
    } else if (describeTarget.isPresent()) {
      sparqlQueryString = "DESCRIBE <" + describeTarget.get() + ">";
      final QueryEngineHTTP qExec = new QueryEngineHTTP(fromEndpoint, sparqlQueryString);
      qExec.setModelContentType(WebContent.contentTypeJSONLD);
      result = qExec.execDescribe();
      qExec.close();
    } else {
      throw new RuntimeException("Neither " + USE_SPARQL_DESCRIBE_OF.toString() + " nor "
        + USE_SPARQL_CONSTRUCT.toString() + " defined to read dataset from " + fromEndpoint
        + ", exit with error.");
    }
    logger.info("Dataset reading is done in {}ms, {} triples found.",
      (System.currentTimeMillis() - startTime), result.size());
    return result;
  }
}
