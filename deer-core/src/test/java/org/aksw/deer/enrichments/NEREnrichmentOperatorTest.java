package org.aksw.deer.enrichments;

import org.aksw.deer.vocabulary.FOXO;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class NEREnrichmentOperatorTest {

  private NEREnrichmentOperator op;
  private Model input, expected;
  private ValidatableParameterMap expectedParameters;

  @Before
  public void setUp() {
    input = ModelFactory.createDefaultModel();
    expected = ModelFactory.createDefaultModel();
    input.read(new StringReader(
      "@prefix ex: <http://example.org/> ." +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." +
        "ex:subject rdfs:comment \"The University of Leipzig has been founded in 1409.\" ."
    ), null, "TTL");

    expected.add(input);
    expected.read(new StringReader(
      "@prefix ex: <http://example.org/> ." +
        "@prefix foxo:  <http://ns.aksw.org/fox/ontology#> ." +
        "ex:subject foxo:relatedTo <http://dbpedia.org/resource/Leipzig> ."
    ), null, "TTL");
    op = new NEREnrichmentOperator();
    expectedParameters = op.createParameterMap()
      .add(NEREnrichmentOperator.LITERAL_PROPERTY, RDFS.comment)
      .add(NEREnrichmentOperator.IMPORT_PROPERTY, FOXO.RELATED_TO)
      .add(NEREnrichmentOperator.NE_TYPE, expected.createLiteral("all"))
      .add(NEREnrichmentOperator.FOX_URL, expected.createResource("http://localhost:4444/fox"))
      .add(NEREnrichmentOperator.PARALLELISM, expected.createTypedLiteral(5))
      .init();
    op.initDegrees(1,1);
    op.initPluginId(ResourceFactory.createResource("urn:ex/test/ner-test"));
  }

  @Test
  public void safeApply() {
    op.initParameters(expectedParameters);
    Model actual = op.apply(List.of(input)).get(0);
    assertTrue("It should detect all types.", expected.isIsomorphicWith(actual));
  }

  @Test
  public void predictApplicability() {
    double applicability = op.predictApplicability(List.of(input), expected);
    assertEquals("It should detect perfect applicability.", 1d, applicability, 0.01d);
  }

  @Test
  public void learn() {
    ValidatableParameterMap actualLearned = op.learnParameterMap(List.of(input), expected, null);
    Resource id = op.getId();
    assertTrue("It should learnParameterMap the standard configuration in all cases", actualLearned.parametrize(id).isIsomorphicWith(expectedParameters.parametrize(id)));
  }

}