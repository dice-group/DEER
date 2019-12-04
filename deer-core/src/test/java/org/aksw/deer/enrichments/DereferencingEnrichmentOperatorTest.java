package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DereferencingEnrichmentOperatorTest {

  private static int PORT;
  private static String EX;
  private static String CFG = "http://example.org/";

  private DereferencingEnrichmentOperator op;
  private Model input, expected;
  private ValidatableParameterMap expectedParameters;
  private FusekiServer fusekiServer;

  @Before
  public void setUp() {
    PORT = WebLib.choosePort();
    EX = "http://localhost:" + PORT + "/";
    DereferencingEnrichmentOperator.setDefaultLookupPrefix(EX);
    input = ModelFactory.createDefaultModel();
    expected = ModelFactory.createDefaultModel();
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
    op = new DereferencingEnrichmentOperator();
    expectedParameters = op.createParameterMap();
    expectedParameters.add(DereferencingEnrichmentOperator.OPERATION, expectedParameters.createResource()
      .addProperty(DereferencingEnrichmentOperator.DEREFERENCING_PROPERTY, lookup.createProperty(EX + "brinellHardness"))
      .addProperty(DereferencingEnrichmentOperator.LOOKUP_PREFIX, EX)
    ).init();
    op.initDegrees(1,1);
    op.initPluginId(ResourceFactory.createResource("urn:ex/test/dereferencing-test"));
  }

  private FusekiServer setupServer(Resource...rest) {
    FusekiServer.Builder builder = FusekiServer.create();
    Arrays.stream(rest).forEach(resource -> {
      String uri = resource.getURI();
      builder.add("/" + uri.substring(EX.length(), uri.indexOf("?")), DatasetFactory.create(resource.getModel()));
    });
    FusekiServer server = builder.port(PORT).build().start();
    Lib.sleep(100);
    return server;
  }

  @After
  public void tearDown() {
    fusekiServer.stop();
  }

  @Test
  public void lookupByPrefix() {
    Model conf = ModelFactory.createDefaultModel();
    Resource deoConf = conf.createResource(CFG + "deo");
    Resource opsEntry = conf.createResource(AnonId.create());
    conf.add(deoConf, DereferencingEnrichmentOperator.OPERATION, opsEntry);
    conf.add(opsEntry, DereferencingEnrichmentOperator.DEREFERENCING_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.IMPORT_PROPERTY, conf.createProperty(CFG + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.LOOKUP_PROPERTY, conf.createProperty(CFG + "madeOf"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.LOOKUP_PREFIX, EX);

    Model in = ModelFactory.createDefaultModel();
    in.add(in.createResource(CFG + "table"),
      in.createProperty(CFG + "madeOf"),
      in.createResource(EX + "birch?default"));

    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output", out.contains(in.createResource(CFG + "table"), ResourceFactory.createProperty(CFG + "brinellHardness"), ResourceFactory.createTypedLiteral(27)));
  }


  @Test
  public void lookupByPrefixAndProperty() {
    Model conf = ModelFactory.createDefaultModel();
    Resource deoConf = conf.createResource(CFG + "deo");
    Resource opsEntry = conf.createResource(AnonId.create());
    conf.add(deoConf, DereferencingEnrichmentOperator.OPERATION, opsEntry);
    conf.add(opsEntry, DereferencingEnrichmentOperator.DEREFERENCING_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.IMPORT_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.LOOKUP_PREFIX, EX);

    Model in = ModelFactory.createDefaultModel();
    in.add(in.createResource(EX + "table"),
      in.createProperty(EX + "madeOf"),
      in.createResource(EX + "birch?default"));

    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output", out.contains(in.createResource(EX + "table"), ResourceFactory.createProperty(EX + "brinellHardness"), ResourceFactory.createTypedLiteral(27)));
  }

  @Test
  public void lookupMultiplePrefix() {
    Model conf = ModelFactory.createDefaultModel();
    Resource deoConf = conf.createResource(CFG + "deo");
    Resource opsEntry = conf.createResource(AnonId.create());
    Resource opsEntry2 = conf.createResource(AnonId.create());
    conf.add(deoConf, DereferencingEnrichmentOperator.OPERATION, opsEntry);
    conf.add(deoConf, DereferencingEnrichmentOperator.OPERATION, opsEntry2);
    conf.add(opsEntry, DereferencingEnrichmentOperator.DEREFERENCING_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.IMPORT_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry, DereferencingEnrichmentOperator.LOOKUP_PREFIX, EX);
    conf.add(opsEntry2, DereferencingEnrichmentOperator.DEREFERENCING_PROPERTY, conf.createProperty(EX + "brinellHardness"));
    conf.add(opsEntry2, DereferencingEnrichmentOperator.IMPORT_PROPERTY, conf.createProperty(EX + "brinellHardness2"));
    conf.add(opsEntry2, DereferencingEnrichmentOperator.LOOKUP_PREFIX, EX);

    Model in = ModelFactory.createDefaultModel();
    in.add(in.createResource(EX + "table"),
      in.createProperty(EX + "madeOf"),
      in.createResource(EX + "birch?default"));

    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output",
         out.contains(in.createResource(EX + "table"), ResourceFactory.createProperty(EX + "brinellHardness"), ResourceFactory.createTypedLiteral(27))
      && out.contains(in.createResource(EX + "table"), ResourceFactory.createProperty(EX + "brinellHardness2"), ResourceFactory.createTypedLiteral(27)) );
  }

//  @Test
//  public void safeApply() {
//    op.initParameters(expectedParameters);
//    Model actual = op.apply(List.of(input)).get(0);
//    assertTrue("It should dereference birch authority.", expected.isIsomorphicWith(actual));
//  }

  @Test
  public void predictApplicability() {
    double applicability = op.predictApplicability(List.of(input), expected);
    assertEquals("It should detect perfect applicability.", 1.0d, applicability, 0.01d);
  }

  @Test
  public void learnParameterMap() {
    ValidatableParameterMap actualLearned = op.learnParameterMap(List.of(input), expected, null);
    Resource id = op.getId();
    assertTrue("It should learn the appropriate configuration", actualLearned.parametrize(id).isIsomorphicWith(expectedParameters.parametrize(id)));
  }

  @Test
  public void reverseApply() {
    Model actual = op.reverseApply(List.of(input), expected).get(0);
    assertTrue("It should revert the operation.", input.isIsomorphicWith(actual));
  }

}