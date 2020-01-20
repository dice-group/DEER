package org.aksw.deer.io;

import com.google.common.collect.Lists;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SparqlModelWriterTest
{
  private static int PORT;
  private static String GSP_ENDPOINT;
  private static String SPARQL_ENDPOINT;
  private static String CFG = "http://example.org/";
  private FusekiServer fusekiServer;
  private static final String NS = "urn:example:";
  private static final String TEST_GRAPH = NS + "testGraph";

  @Before
  public void setUp()
  {
    PORT = WebLib.choosePort();
    GSP_ENDPOINT = "http://localhost:" + PORT + "/default/data";
    SPARQL_ENDPOINT = "http://localhost:" + PORT + "/default/update";
    fusekiServer = FusekiServer.make(PORT, "default", DatasetFactory.create().asDatasetGraph());
    fusekiServer.start();
    Lib.sleep(500);
  }

  @After
  public void tearDown() {
    fusekiServer.stop();
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, "");

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "tentris?running"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "iguana?created"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "dss?default"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "updatedBy"),
      testModel.createResource(NS + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, TEST_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "tentris?running"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "iguana?created"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "dss?default"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "updatedBy"),
      testModel.createResource(NS + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH);
    connection.delete(TEST_GRAPH);
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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, TEST_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH);
    connection.delete(TEST_GRAPH);
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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SPARQL_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "tentris?running"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "iguana?created"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "dss?default"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "updatedBy"),
      testModel.createResource(NS + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SPARQL_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, TEST_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Create the testModel which looks likes merge of firstModel and secondModel to test merge is working or not.
    Model testModel = ModelFactory.createDefaultModel();
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "tentris?running"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "iguana?created"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "madeOf"),
      testModel.createResource(NS + "dss?default"));
    testModel.add(testModel.createResource(NS + "table"),
      testModel.createProperty(NS + "updatedBy"),
      testModel.createResource(NS + "dice?update"));

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH);
    connection.delete(TEST_GRAPH);
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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SPARQL_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEAFULT_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SPARQL_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, TEST_GRAPH);

    //Create the first model to write it into fuseki.
    Model firstModel = ModelFactory.createDefaultModel();
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "dss?default"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "updatedBy"),
      firstModel.createResource(NS + "dice?update"));
    firstModel.add(firstModel.createResource(NS + "table"),
      firstModel.createProperty(NS + "madeOf"),
      firstModel.createResource(NS + "tentris?running"));

    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initParameters(writer.createParameterMap().populate(mainRes).init());
    writer.initPluginId(mainRes); writer.initDegrees(1, 1);

    //Write the first model into fuseki.
    Model out = writer.safeApply(Lists.newArrayList(firstModel)).get(0);

    //Create the second model to write it into fuseki.
    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(NS + "table"),
      secondModel.createProperty(NS + "madeOf"),
      secondModel.createResource(NS + "iguana?created"));

    //Write the second model into fuseki.
    out = writer.safeApply(Lists.newArrayList(secondModel)).get(0);

    //Get the model from fuseki server.
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(GSP_ENDPOINT);

    RDFConnection connection = builder.build();
//    Dataset testDS = connection.fetchDataset();
    Model checkModel = connection.fetch(TEST_GRAPH);
    connection.delete(TEST_GRAPH);
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }
}