package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Extension
public class SparqlModelWriter extends AbstractModelWriter {

  public static final Property ENDPOINT = DEER.property("endPoint");
  public static final Property GSP_ENDPOINT = DEER.property("gspEndPoint");
  public static final Property QUERY_ENDPOINT = DEER.property("queryEndPoint");
  public static final Property WRITE_TYPE = DEER.property("writeType");
  public static final Property WRITE_OP = DEER.property("writeOp");
  public static final Property GRAPH_NAME = DEER.property("graphName");
  public static final String REPLACE = "replace";
  public static final String DEFAULT_GRAPH = "default";
  public static final String SPARQL = "sparql";
  public static final String GRAPH_STORE_HTTP = "graphstore-http";
  public static final String MERGE = "merge";
  private static final Logger logger = LoggerFactory.getLogger(SparqlModelWriter.class);
  private RDFConnection connection;

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(WRITE_TYPE)
      .declareProperty(WRITE_OP)
      .declareProperty(ENDPOINT)
      .declareProperty(GSP_ENDPOINT)
      .declareProperty(QUERY_ENDPOINT)
      .declareProperty(GRAPH_NAME)
      .declareValidationShape(getValidationModelFor(SparqlModelWriter.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return ExecutionNode.toMultiExecution(this::write).apply(data);
  }

  private Model write(Model model) {
    Optional<String> writeType = getParameterMap().getOptional(WRITE_TYPE)
      .map(RDFNode::asLiteral).map(Literal::getString);
    Optional<String> writeOp = getParameterMap().getOptional(WRITE_OP)
      .map(RDFNode::asLiteral).map(Literal::getString);
    Optional<String> endPoint = getParameterMap().getOptional(ENDPOINT)
      .map(RDFNode::asResource).map(Resource::getURI);
    Optional<String> graphName = getParameterMap().getOptional(GRAPH_NAME)
      .map(RDFNode::toString);
    Optional<String> gspEndpoint = getParameterMap().getOptional(GSP_ENDPOINT)
      .map(RDFNode::asResource).map(Resource::getURI);
    Optional<String> queryEndpoint = getParameterMap().getOptional(QUERY_ENDPOINT)
      .map(RDFNode::asResource).map(Resource::getURI);
/*
    String nullStr = null;
    writeType = Optional.ofNullable(nullStr);
    writeOp = Optional.ofNullable(nullStr);
 //   endPoint = Optional.ofNullable(nullStr);
    graphName = Optional.ofNullable(nullStr);
*/
    if (writeType.isEmpty()) {
      logger.info("Writing protocol is null, switching to Graph-store HTTP protocol");
      writeType = Optional.of(GRAPH_STORE_HTTP);
    }

    if (writeOp.isEmpty()) {
      logger.info("Writing operation type is null, switching to Merge operation");
      writeOp = Optional.of(MERGE);
    }

    if (endPoint.isEmpty()) {
      logger.info("Endpoint is not specified, exiting without writing.");
      return model;
    }

    if (graphName.isEmpty()) {
      logger.info("Graph name is null, switching to Default Graph");
      graphName = Optional.of(DEFAULT_GRAPH);
    }

    if (gspEndpoint.isEmpty()) {
      logger.info("Graph name is null, switching to Default Graph");
      gspEndpoint = Optional.of("data");
    }

    if (queryEndpoint.isEmpty()) {
      logger.info("Graph name is null, switching to Default Graph");
      queryEndpoint = Optional.of("update");
    }

    getConnection(endPoint.get(), gspEndpoint.get(), queryEndpoint.get());

    if (writeType.get().equals(SPARQL)) {
      sparqlWrite(model, endPoint.get(), writeOp.get(), graphName.get());
    } else if (writeType.get().equals(GRAPH_STORE_HTTP)) {
      httpWrite(model, endPoint.get(), writeOp.get(), graphName.get());
    } //@todo: what happens when a bad writeType is given?

    return model;
  }

  private void getConnection(String endPoint, String gspEndpoint, String queryEndpoint) {
    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    Credentials credentials = new UsernamePasswordCredentials("admin", "password");
    credsProvider.setCredentials(AuthScope.ANY, credentials);
    HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(endPoint)
      .queryEndpoint(queryEndpoint)
      .gspEndpoint(gspEndpoint)
      .httpClient(client);

    connection = builder.build();
  }

  private String getGraphData(Model model) {
    Writer writer = new StringWriter();
    model.write(writer, "TRIG");
    return writer.toString();
  }

  /**
   * Implementation of SparQL Writer using PURE SPARQL UPDATE protocol.
   */
  private void sparqlWrite(Model model, String endPoint, String writeOp, String graphName) {
    try {
      if (writeOp.equals(SparqlModelWriter.MERGE)) {
        if (graphName.equals(DEFAULT_GRAPH) || graphName.equals("")) {
          logger.info("Writing the model with [pure SPARQL UPDATE, MERGE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.update("INSERT DATA {" + getGraphData(model) + "}");
        } else {
          logger.info("Writing the model with [pure SPARQL UPDATE, MERGE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.update("INSERT DATA { GRAPH <" + graphName + "> {" + getGraphData(model) + "} }");
        }
      } else if (writeOp.equals(SparqlModelWriter.REPLACE)) {
        if (graphName.equals(DEFAULT_GRAPH) || graphName.equals("")) {
          logger.info("Writing the model with [pure SPARQL UPDATE, REPLACE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.update("CLEAR DEFAULT");
          connection.update("INSERT DATA {" + getGraphData(model) + "}");
        } else {
          logger.info("Writing the model with [pure SPARQL UPDATE, REPLACE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.update("CLEAR GRAPH <" + graphName + ">");
          connection.update("INSERT DATA { GRAPH <" + graphName + "> {" + getGraphData(model) + "} }");
        }
      }
      connection.commit();
      connection.close();
    } catch (Exception e) {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        endPoint, e);
    }
  }

  /**
   * Implementation of SparQL Writer using Graph-Store HTTP protocol.
   */
  private void httpWrite(Model model, String endPoint, String writeOp, String graphName) {
    try {
      if (writeOp.equals(SparqlModelWriter.MERGE)) {
        if (graphName.equals(DEFAULT_GRAPH) || graphName.equals("")) {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.load(model);
        } else {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.load(graphName, model);
        }
      } else if (writeOp.equals(SparqlModelWriter.REPLACE)) {
        if (graphName.equals(DEFAULT_GRAPH) || graphName.equals("")) {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.put(model);
        } else {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.put(graphName, model);
        }
      }

      connection.commit();
      connection.close();
    } catch (Exception e) {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        endPoint, e);
    }
  }
}
