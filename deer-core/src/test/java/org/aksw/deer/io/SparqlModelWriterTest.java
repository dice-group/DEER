package org.aksw.deer.io;

import com.google.common.collect.Lists;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.eclipse.jetty.http.MultiPartParser;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class SparqlModelWriterTest {
  private static int PORT;
  private static String DATASET_ENDPOINT;
  private static String GSP_ENDPOINT;
  private static String SPARQL_ENDPOINT;
  private static String CFG = "http://example.org/";
  private FusekiServer fusekiServer;
  private Server server;
  private static final String NS = "urn:example:";
  private static final String TEST_GRAPH = NS + "testGraph";

  @Before
  public void setUp() throws Exception {
    PORT = WebLib.choosePort();
    DATASET_ENDPOINT = "http://localhost:" + PORT + "/default/";
    GSP_ENDPOINT = "data";
    SPARQL_ENDPOINT = "update";
    fusekiServer = FusekiServer.make(PORT, "default", DatasetFactory.create().asDatasetGraph());

    server = fusekiServer.getJettyServer();

    String realmResourceName = "etc/realm.properties";
    ClassLoader classLoader = SparqlModelWriterTest.class.getClassLoader();
    URL realmProps = classLoader.getResource(realmResourceName);
    if (realmProps == null)
      throw new FileNotFoundException("Unable to find " + realmResourceName);

    LoginService loginService = new HashLoginService("MyRealm",
      realmProps.toExternalForm());
    server.addBean(loginService);

    ConstraintSecurityHandler security = new ConstraintSecurityHandler();
    server.setHandler(security);

    Constraint constraint = new Constraint();
    constraint.setName("auth");
    constraint.setAuthenticate(true);
    constraint.setRoles(new String[]{"user", "admin"});

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setPathSpec("/*");
    mapping.setConstraint(constraint);

    security.setConstraintMappings(Collections.singletonList(mapping));
    security.setAuthenticator(new BasicAuthenticator());
    security.setLoginService(loginService);

    security.setHandler(new Handler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("Handle");
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        BufferedReader reader = request.getReader();

        System.out.println(request.getContentType());
        String readLine = reader.readLine();
        String res = "";
        while(readLine != null) {

          res += readLine + "\n";
          readLine = reader.readLine();
        }

        System.out.println(res);
        response.getWriter().println(res);
      }

      @Override
      public void setServer(Server server) {
        System.out.println("set server");
      }

      @Override
      public Server getServer() {
        System.out.println("getserver");
        return null;
      }

      @Override
      public void destroy() {
        System.out.println("destroy server");
      }

      @Override
      public void start() throws Exception {
        System.out.println("start");
      }

      @Override
      public void stop() throws Exception {
        System.out.println("stop");
      }

      @Override
      public boolean isRunning() {
        System.out.println("is running?");
        return false;
      }

      @Override
      public boolean isStarted() {
        System.out.println("is started");
        return false;
      }

      @Override
      public boolean isStarting() {
        System.out.println("is starting?");
        return false;
      }

      @Override
      public boolean isStopping() {
        System.out.println("is stopping?");
        return false;
      }

      @Override
      public boolean isStopped() {
        System.out.println("ius stopped?");
        return false;
      }

      @Override
      public boolean isFailed() {
        System.out.println("is failed");
        return false;
      }

      @Override
      public void addLifeCycleListener(Listener listener) {
      }

      @Override
      public void removeLifeCycleListener(Listener listener) {
      }
    });

      server.start();
    System.out.println(DATASET_ENDPOINT);


    Lib.sleep(500);
  }

  @After
  public void tearDown() throws Exception {
    fusekiServer.stop();
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

    Scanner sc = new Scanner(System.in);
    sc.next();

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

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
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

    RDFConnection connection = builder.build();
//    Dataset testDS = connection.fetchDataset();
    Model checkModel = connection.fetch(TEST_GRAPH);
    connection.delete(TEST_GRAPH);
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }
/*
  @Test
  public void switchToDefault() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.SPARQL);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, DATASET_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.GSP_ENDPOINT, GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.QUERY_ENDPOINT, SPARQL_ENDPOINT);
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
      .destination(DATASET_ENDPOINT + GSP_ENDPOINT);

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }*/
}