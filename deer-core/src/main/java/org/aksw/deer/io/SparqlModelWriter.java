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

import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 *
 */
@Extension
public class SparqlModelWriter extends AbstractModelWriter {

  public static final Property ENDPOINT = DEER.property("endpoint");
  public static final Property WRITE_TYPE = DEER.property("writeType");
  public static final Property WRITE_OP = DEER.property("writeOp");
  public static final Property GRAPH_NAME = DEER.property("graphName");
  public static final Property CRED_FILE = DEER.property("credentialsFile");
  public static final Property CRED = DEER.property("credentials");
  public static final Property USER = DEER.property("user");
  public static final Property PW = DEER.property("password");

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
      .declareProperty(GRAPH_NAME)
      .declareProperty(CRED_FILE)
      .declareProperty(CRED)
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

    getConnection(endPoint.get());

    if (writeType.get().equals(SPARQL)) {
      sparqlWrite(model, endPoint.get(), writeOp.get(), graphName.get());
    } else if (writeType.get().equals(GRAPH_STORE_HTTP)) {
      httpWrite(model, endPoint.get(), writeOp.get(), graphName.get());
    } else {
      logger.info("Invalid Writing operation type, switching to Graph-Store HTTP protocol.");
      httpWrite(model, endPoint.get(), writeOp.get(), graphName.get());
    }

    return model;
  }

  private void getConnection(String endPoint) {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(endPoint);

    Optional<String> credFile = getParameterMap().getOptional(CRED_FILE)
      .map(RDFNode::toString);
    Optional<Resource> credentialsResource = getParameterMap().getOptional(CRED)
      .map(RDFNode::asResource);

    String user = null;
    String pw = null;
    if (credentialsResource.isPresent()) {
      user = credentialsResource.get().getProperty(USER).getString();
      pw = credentialsResource.get().getProperty(PW).getString();
    } else if (credFile.isPresent()) {
      String credPath = injectWorkingDirectory(credFile.get());
      Properties prop = new Properties();
      try {
        prop.load(new FileInputStream(credPath));
        user = prop.getProperty("username");
        pw = prop.getProperty("password");
      } catch (Exception ex) {
        throw new RuntimeException("Encountered problem while trying to read credential file " +
          endPoint, ex);
      }
    }
    if (Objects.nonNull(user) && Objects.nonNull(pw)) {
      BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
      Credentials credentials = new UsernamePasswordCredentials(user, pw);
      credsProvider.setCredentials(AuthScope.ANY, credentials);
      HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
      builder.httpClient(client);
    }
    connection = builder.build();
  }

  private String getGraphData(Model model) {
    Writer writer = new StringWriter();
    model.write(writer, "NT");
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
