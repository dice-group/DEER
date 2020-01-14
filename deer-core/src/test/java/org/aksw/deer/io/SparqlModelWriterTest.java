package org.aksw.deer.io;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

public class SparqlModelWriterTest
{
  private static int PORT;
  private static String EX;
  private static String CFG = "http://example.org/";

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

    FusekiServer fusekiServer = setupServer(birch);
    expected.add(input);
    expected.add(lookup);
    SparqlModelWriter writer = new SparqlModelWriter();
    writer.initDegrees(1,1);
    writer.initPluginId(ResourceFactory.createResource("urn:ex/test/dereferencing-test"));
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
  public void gspWithMerge() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRs = conf.createResource(CFG + "deo");
    conf.add(mainRs, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRs, SparqlModelWriter.WRITE_OP, SparqlModelWriter.MERGE);
    conf.add(mainRs, SparqlModelWriter.ENDPOINT, "http://localhost:3030/");

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
    writer.initParameters(writer.createParameterMap().populate(mainRs).init());
    writer.initPluginId(mainRs); writer.initDegrees(1, 1);

    writer.write(firstModel);

    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    writer.write(secondModel);
  }
  @Test
  public void gspWithReplace() {
    Model conf = ModelFactory.createDefaultModel();
    Resource mainRs = conf.createResource(CFG + "deo");
    conf.add(mainRs, SparqlModelWriter.WRITE_TYPE, SparqlModelWriter.GRAPH_STORE_HTTP);
    conf.add(mainRs, SparqlModelWriter.WRITE_OP, SparqlModelWriter.REPLACE);
    conf.add(mainRs, SparqlModelWriter.ENDPOINT, "http://localhost:3030/");

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
    writer.initParameters(writer.createParameterMap().populate(mainRs).init());
    writer.initPluginId(mainRs); writer.initDegrees(1, 1);

    writer.write(firstModel);

    Model secondModel = ModelFactory.createDefaultModel();
    secondModel.add(secondModel.createResource(EX + "table"),
      secondModel.createProperty(EX + "madeOf"),
      secondModel.createResource(EX + "iguana?created"));

    writer.write(secondModel);
  }
}