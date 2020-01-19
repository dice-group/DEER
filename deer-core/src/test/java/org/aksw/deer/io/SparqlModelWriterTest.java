package org.aksw.deer.io;

import com.google.common.collect.Lists;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;

public class SparqlModelWriterTest
{
  private static int PORT;
  private static String EX;
  private static String CFG = "http://example.org/";
  private FusekiServer fusekiServer;

  @Before
  public void setUp()
  {
    PORT = WebLib.choosePort();
    EX = "http://localhost:" + PORT + "/";

    Model input = ModelFactory.createDefaultModel();
    Model expected = ModelFactory.createDefaultModel();
    input.read(new StringReader(
      "@prefix ex: <" + EX + "> ." +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." +
        "ex:subject rdfs:seeAlso <" + EX + "birch?default> ."
    ), null, "TTL");
    Model lookup = ModelFactory.createDefaultModel();
    Resource birch = lookup.createResource(EX + "birch?default");
    lookup.add(birch,
      lookup.createProperty(EX + "brinellHardness"),
      lookup.createTypedLiteral(27));

    fusekiServer = setupServer(birch);
    expected.add(input);
    expected.add(lookup);
    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initDegrees(1,1);
    writer.initPluginId(ResourceFactory.createResource("urn:ex/test/dereferencing-test"));
  }

  @After
  public void tearDown() {
    fusekiServer.stop();
  }

  private FusekiServer setupServer(Resource...rest)
  {
    FusekiServer.Builder builder = FusekiServer.create();
    Arrays.stream(rest).forEach(resource -> {
      String uri = resource.getURI();
      builder.add("/" + uri.substring(EX.length(), uri.indexOf("?")), DatasetFactory.create(resource.getModel()));
    });
    FusekiServer server = builder.port(PORT).build().start();
    Lib.sleep(100);
    return server;
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "tentris?running"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "iguana?created"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "dss?default"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "updatedBy"),
      testModel.createResource(EX + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }

  @Test
  public void writeToDefaultGraphWihReplaceAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }

  @Test
  public void writeToNamedGraphWihMergeAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "testGraph");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "tentris?running"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "iguana?created"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "dss?default"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "updatedBy"),
      testModel.createResource(EX + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch("testGraph");
    connection.delete("testGraph");
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }

  @Test
  public void writeToNamedGraphWihReplaceAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "testGraph");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch("testGraph");
    connection.delete("testGraph");
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }

  @Test
  public void writeToDefaultGraphWihMergeAndSPARQL() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.SPARQL);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "tentris?running"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "iguana?created"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "dss?default"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "updatedBy"),
      testModel.createResource(EX + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }

  @Test
  public void writeToNamedGraphWihMergeAndSPARQL() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.SPARQL);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "testGraph");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "tentris?running"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "iguana?created"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "madeOf"),
      testModel.createResource(EX + "dss?default"));
    testModel.add(testModel.createResource(EX + "table"),
      testModel.createProperty(EX + "updatedBy"),
      testModel.createResource(EX + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch("testGraph");
    connection.delete("testGraph");
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }

  @Test
  public void writeToDefaultGraphWihReplaceAndSPARQL() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.SPARQL);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }

  @Test
  public void writeToNamedGraphWihReplaceAndSPARQL() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.SPARQL);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, "http://localhost:3030/test/");
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "testGraph");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "dss?default"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "updatedBy"),
      firstModel.createResource(EX + "dice?update"));
    firstModel.add(firstModel.createResource(EX + "table"),
      firstModel.createProperty(EX + "madeOf"),
      firstModel.createResource(EX + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination("http://localhost:3030/test");

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch("testGraph");
    connection.delete("testGraph");
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }
}