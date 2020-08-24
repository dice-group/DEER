package org.aksw.deer.io;

import com.google.common.collect.Lists;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class SparqlModelWriterTest {
  private static int PORT;
  private static int SECURED_PORT;
  private static Resource DATASET_ENDPOINT;
  private static Resource SECURED_DATASET_ENDPOINT;
  private static Resource GSP_ENDPOINT;
  private static Resource SECURED_GSP_ENDPOINT;
  private static Resource SPARQL_ENDPOINT;
  private static Resource SECURED_SPARQL_ENDPOINT;
  private static String CFG = "http://example.org/";
  private FusekiServer fusekiServer;
  private FusekiServer securedFusekiServer;

  private static final String NS = "urn:example:";
  private static final Resource TEST_GRAPH = ResourceFactory.createResource(NS + "testGraph");

  @Before
  public void setUp() throws Exception {
    PORT = WebLib.choosePort();
    SECURED_PORT = WebLib.choosePort();
    DATASET_ENDPOINT = ResourceFactory.createResource("http://localhost:" + PORT + "/default/");
    SECURED_DATASET_ENDPOINT = ResourceFactory.createResource("http://localhost:" + SECURED_PORT + "/default/");
    GSP_ENDPOINT = ResourceFactory.createResource(DATASET_ENDPOINT.getURI() + "data");
    SECURED_GSP_ENDPOINT = ResourceFactory.createResource(SECURED_DATASET_ENDPOINT.getURI() + "data");
    SPARQL_ENDPOINT = ResourceFactory.createResource(DATASET_ENDPOINT.getURI() + "update");
    SECURED_SPARQL_ENDPOINT = ResourceFactory.createResource(SECURED_DATASET_ENDPOINT.getURI() + "update");
    fusekiServer = FusekiServer.make(PORT, "default", DatasetFactory.create().asDatasetGraph());
    securedFusekiServer = FusekiServer.make(SECURED_PORT, "default", DatasetFactory.create().asDatasetGraph());
    Server server = securedFusekiServer.getJettyServer();

    String realmResourceName = "etc/realm.properties";
    ClassLoader classLoader = SparqlModelWriterTest.class.getClassLoader();
    URL realmProps = classLoader.getResource(realmResourceName);
    if (realmProps == null)
      throw new FileNotFoundException("Unable to find " + realmResourceName);

    LoginService loginService = new HashLoginService("MyRealm",
      realmProps.toExternalForm());
    server.addBean(loginService);

    ConstraintSecurityHandler security = new ConstraintSecurityHandler();
    Handler tempHandler = server.getHandler();
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


    security.setHandler(tempHandler);

    fusekiServer.start();
    server.start();
//    System.out.println(DATASET_ENDPOINT);
//    System.out.println(SECURED_DATASET_ENDPOINT);
    Lib.sleep(500);
  }

  @After
  public void tearDown() throws Exception {
    fusekiServer.stop();
    securedFusekiServer.stop();
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSP() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, GSP_ENDPOINT);

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
      .destination(GSP_ENDPOINT.getURI());

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
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEFAULT_GRAPH);

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
      .destination(GSP_ENDPOINT.getURI());

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
      .destination(GSP_ENDPOINT.getURI());

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH.getURI());
    connection.delete(TEST_GRAPH.getURI());
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
      .destination(GSP_ENDPOINT.getURI());

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH.getURI());
    connection.delete(TEST_GRAPH.getURI());
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
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEFAULT_GRAPH);

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
      .destination(GSP_ENDPOINT.getURI());

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
      .destination(GSP_ENDPOINT.getURI());

    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch(TEST_GRAPH.getURI());
    connection.delete(TEST_GRAPH.getURI());
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
    conf.add(mainRes, SparqlModelWriter.GRAPH_NAME, SparqlModelWriter.DEFAULT_GRAPH);

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
      .destination(GSP_ENDPOINT.getURI());

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
      .destination(GSP_ENDPOINT.getURI());

    RDFConnection connection = builder.build();
//    Dataset testDS = connection.fetchDataset();
    Model checkModel = connection.fetch(TEST_GRAPH.getURI());
    connection.delete(TEST_GRAPH.getURI());
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(secondModel));
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSPUsingAuthenticationWithFile() throws URISyntaxException, MalformedURLException {
    URL credURL = getClass().getClassLoader().getResource("credentials");
    String credPath = Paths.get("").toAbsolutePath().toUri().relativize(credURL.toURI()).toString();

    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SECURED_GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.CRED_FILE, credPath);


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

    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    Credentials credentials = new UsernamePasswordCredentials("ranjithk", "admin");
    credsProvider.setCredentials(AuthScope.ANY, credentials);
    HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(SECURED_GSP_ENDPOINT.getURI())
      .httpClient(client);



    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    //connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }

  @Test
  public void writeToDefaultGraphWihMergeAndGSPUsingAuthenticationWithCredentials() throws URISyntaxException, MalformedURLException {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRes = conf.createResource(CFG + "deo");
    Resource credResource = conf.createResource();
    conf.add(mainRes, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRes, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRes, SparqlModelWriter.ENDPOINT, SECURED_GSP_ENDPOINT);
    conf.add(mainRes, SparqlModelWriter.CRED, credResource);
    conf.add(credResource, SparqlModelWriter.USER, "root");
    conf.add(credResource, SparqlModelWriter.PW, "admin123");


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

    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    Credentials credentials = new UsernamePasswordCredentials("ranjithk", "admin");
    credsProvider.setCredentials(AuthScope.ANY, credentials);
    HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create()
      .destination(SECURED_GSP_ENDPOINT.getURI())
      .httpClient(client);



    RDFConnection connection = builder.build();
    Model checkModel = connection.fetch();
    //connection.delete();
    connection.commit();
    connection.close();
    //assert if the testModel and model from fuseki server is not same.
    assertTrue(checkModel.isIsomorphicWith(testModel));
  }
}