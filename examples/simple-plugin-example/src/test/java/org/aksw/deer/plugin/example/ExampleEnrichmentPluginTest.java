package org.aksw.deer.plugin.example;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 *
 *
 *
 */
public class ExampleEnrichmentPluginTest {

  @Test
  public void TestExampleEnrichmentOperator() {
    Model expected = ModelFactory.createDefaultModel();
    expected.setNsPrefix("deer", "http://aksw.org/deer/ontology#");
    expected.add(ResourceFactory.createResource("http://aksw.org/deer/ontology#examplePlugin"),
      ResourceFactory.createProperty("http://aksw.org/deer/ontology#says"),
      ResourceFactory.createPlainLiteral("Hello World!"));
    ExampleEnrichmentPlugin.ExampleEnrichmentOperator eeo = new ExampleEnrichmentPlugin.ExampleEnrichmentOperator();
    eeo.initPluginId(ResourceFactory.createResource("urn:example-enrichment-operator"));
    eeo.initDegrees(1, 1);
    Model result = eeo.safeApply(List.of(ModelFactory.createDefaultModel())).get(0);
    assertTrue("The empty model enriched by the example enrichment operator should contain exactly" +
        " one triple: 'deer:examplePlugin deer:says \"Hello World!\"'.",
      result.isIsomorphicWith(expected));
  }

}