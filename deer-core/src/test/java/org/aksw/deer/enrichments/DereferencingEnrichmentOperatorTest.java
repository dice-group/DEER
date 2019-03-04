package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class DereferencingEnrichmentOperatorTest {

  private static int PORT;
  private static String EX;
  private static String CFG = "http://example.org/";

  @Before
  public void prepareServer() {
    PORT = WebLib.choosePort();
    EX = "http://localhost:" + PORT + "/";
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

    Model lookup = ModelFactory.createDefaultModel();
    Resource birch = lookup.createResource(EX + "birch?default");
    lookup.add(birch,
      lookup.createProperty(EX + "brinellHardness"),
      lookup.createTypedLiteral(27));

    setupServer(birch);
    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output", out.contains(in.createResource(CFG + "table"), lookup.createProperty(CFG + "brinellHardness"), lookup.createTypedLiteral(27)));
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

    Model lookup = ModelFactory.createDefaultModel();
    Resource birch = lookup.createResource(EX + "birch?default");
    lookup.add(birch,
      lookup.createProperty(EX + "brinellHardness"),
      lookup.createTypedLiteral(27));

    setupServer(birch);
    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output", out.contains(in.createResource(EX + "table"), lookup.createProperty(EX + "brinellHardness"), lookup.createTypedLiteral(27)));
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

    Model lookup = ModelFactory.createDefaultModel();
    Resource birch = lookup.createResource(EX + "birch?default");
    lookup.add(birch,
      lookup.createProperty(EX + "brinellHardness"),
      lookup.createTypedLiteral(27));

    setupServer(birch);
    DereferencingEnrichmentOperator deo = new DereferencingEnrichmentOperator();
    deo.initParameters(deo.createParameterMap().populate(deoConf).init());
    deo.initPluginId(deoConf); deo.initDegrees(1, 1);
    Model out = deo.safeApply(Lists.newArrayList(in)).get(0);
    //    System.out.println(out);
    assertTrue("The dereferenced data is in the output",
         out.contains(in.createResource(EX + "table"), lookup.createProperty(EX + "brinellHardness"), lookup.createTypedLiteral(27))
      && out.contains(in.createResource(EX + "table"), lookup.createProperty(EX + "brinellHardness2"), lookup.createTypedLiteral(27)) );
  }

}