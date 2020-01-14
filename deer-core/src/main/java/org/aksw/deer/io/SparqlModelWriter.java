package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static final String REPLACE = "replace";
  public static final String SPARQL = "sparql";
  public static final String GRAPH_STORE_HTTP = "graphstore-http";
  public static final String MERGE = "merge";

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(WRITE_TYPE)
      .declareProperty(WRITE_OP)
      .declareProperty(ENDPOINT)
      .declareValidationShape(getValidationModelFor(SparqlModelWriter.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return ExecutionNode.toMultiExecution(this::write).apply(data);
  }

  public Model write(Model model) {
    String writeType = getParameterMap().get(WRITE_TYPE).asLiteral().getString();
    String endPoint = getParameterMap().get(ENDPOINT).asLiteral().getString();

    if(writeType.equals(SPARQL))
    {
      System.out.println("Writing using Pure SPARQL to: " + endPoint);
      sparqlWrite(model);
    }
    else if(writeType.equals(GRAPH_STORE_HTTP))
    {
      System.out.println("Writing using Pure Graphstore http to: " + endPoint);
      httpWrite(model);
    }

    return model;
  }

  public void sparqlWrite(Model model)
  {
    String writeOperation = getParameterMap().get(WRITE_OP).toString();
    System.out.println("Write operation is : " + writeOperation);

    //TODO: implement SparqlModelWriter using Pure SPARQL Update here.
    return;
  }

  public void httpWrite(Model model)
  {
    String writeOperation = getParameterMap().get(WRITE_OP).toString();
    String endPoint = getParameterMap().get(ENDPOINT).toString();

    System.out.println("Write operation is : " + writeOperation);

    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(endPoint)
      .queryEndpoint("sparql")
      .updateEndpoint(null)
      .gspEndpoint("DS");

    RDFConnection connection = builder.build();
    connection.begin(ReadWrite.WRITE);

    if(writeOperation.equals(SparqlModelWriter.MERGE))
      connection.load(model);
    else if(writeOperation.equals(SparqlModelWriter.REPLACE))
      connection.put(model);

    connection.commit();
    connection.close();

    return;
  }
}
