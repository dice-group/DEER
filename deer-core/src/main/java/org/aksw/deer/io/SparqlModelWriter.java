package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 */
@Extension
public class SparqlModelWriter extends AbstractModelWriter
{
  private static final Logger logger = LoggerFactory.getLogger(SparqlModelWriter.class);

  public static final Property ENDPOINT = DEER.property("endPoint");
  public static final Property WRITE_TYPE = DEER.property("writeType");
  public static final Property WRITE_OP = DEER.property("writeOp");
  public static final Property GRAPH_NAME = DEER.property("graphName");

  public static final String REPLACE = "replace";
  public static final String DEAFULT_GRAPH = "default";
  public static final String SPARQL = "sparql";
  public static final String GRAPH_STORE_HTTP = "graphstore-http";
  public static final String MERGE = "merge";

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(WRITE_TYPE)
      .declareProperty(WRITE_OP)
      .declareProperty(ENDPOINT)
      .declareProperty(GRAPH_NAME)
      .declareValidationShape(getValidationModelFor(SparqlModelWriter.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return ExecutionNode.toMultiExecution(this::write).apply(data);
  }

  private Model write(Model model) {
    String writeType = getParameterMap().get(WRITE_TYPE).asLiteral().getString();
    String writeOp = getParameterMap().get(WRITE_OP).asLiteral().getString();
    String endPoint = getParameterMap().get(ENDPOINT).asLiteral().getString();
    String graphName = getParameterMap().get(GRAPH_NAME).asLiteral().getString();

    if(writeType.equals(SPARQL))
    {
      sparqlWrite(model, endPoint, writeOp, graphName);
    }
    else if(writeType.equals(GRAPH_STORE_HTTP))
    {
      httpWrite(model, endPoint, writeOp, graphName);
    }

    return model;
  }


  private String getGraphData(Model model)
  {
    OutputStream output = new OutputStream()
    {
      private StringBuilder string = new StringBuilder();
      @Override
      public void write(int b) throws IOException {
        this.string.append((char) b );
      }

      public String toString(){
        return this.string.toString();
      }
    };

    model.write(output, "TRIG");
    return output.toString();
  }

  private void sparqlWrite(Model model, String endPoint, String writeOp, String graphName)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(endPoint);

    RDFConnection connection = builder.build();

    try
    {
      if(writeOp.equals(SparqlModelWriter.MERGE))
      {
        if(graphName.equals(DEAFULT_GRAPH) || graphName.equals(""))
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.update("INSERT DATA {" + getGraphData(model) + "}");
        }
        else
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.update("INSERT DATA { GRAPH <" + graphName + "> {" + getGraphData(model) + "} }");
        }
      }
      else if(writeOp.equals(SparqlModelWriter.REPLACE))
      {
        if(graphName.equals(DEAFULT_GRAPH) || graphName.equals(""))
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.update("CLEAR DEFAULT");
          connection.update("INSERT DATA {" + getGraphData(model) + "}");
        }
        else
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.update("CLEAR GRAPH <" + graphName + ">");
          connection.update("INSERT DATA { GRAPH <" + graphName + "> {" + getGraphData(model) + "} }");
        }
      }
      connection.commit();
      connection.close();
    }
    catch(Exception e)
    {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        endPoint, e);
    }

    return;
  }

  private Model httpWrite(Model model, String endPoint, String writeOp, String graphName)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(endPoint);

    RDFConnection connection = builder.build();

    try
    {
      if(writeOp.equals(SparqlModelWriter.MERGE))
      {
        if(graphName.equals(DEAFULT_GRAPH) || graphName.equals(""))
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.load(model);
        }
        else
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, MERGE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.load(graphName, model);
        }
      }
      else if(writeOp.equals(SparqlModelWriter.REPLACE))
      {
        if(graphName.equals(DEAFULT_GRAPH) || graphName.equals(""))
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: default] "
            + "to the endpoint: " + endPoint);
          connection.put(model);
        }
        else
        {
          logger.info("Writing the model with [Graph-Store HTTP protocol, REPLACE operation, Graph name: "
            + graphName + "] to the endpoint: " + endPoint);
          connection.put(graphName, model);
        }
      }

      connection.commit();
      connection.close();
    }
    catch(Exception e)
    {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        endPoint, e);
    }

    return model;
  }
}
