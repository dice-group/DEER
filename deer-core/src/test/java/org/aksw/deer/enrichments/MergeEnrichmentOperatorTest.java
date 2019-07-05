package org.aksw.deer.enrichments;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MergeEnrichmentOperatorTest {

  private Model source, target, expected;
  MergeEnrichmentOperator op;

  @Before
  public void setUp() {
    source = ModelFactory.createDefaultModel();
    target = ModelFactory.createDefaultModel();
    expected = ModelFactory.createDefaultModel();
    source.read(new StringReader(
      "@prefix ex: <http://example.org/> ." +
        "ex:subject1 ex:name \"Barack Obama\" ." +
        "ex:subject1 ex:age \"57\" ." +
        "ex:subject2 ex:name \"Donald Trump\" ." +
        "ex:subject2 ex:age \"73\" ."
    ), null, "TTL");
    target.read(new StringReader(
      "@prefix ex: <http://example.org/> ." +
        "ex:subject3 ex:name \"Berack Obama\" ." +
        "ex:subject3 ex:age \"56\" ." +
        "ex:subject4 ex:name \"Donald Drump\" ." +
        "ex:subject4 ex:age \"78\" ."
    ), null, "TTL");

    expected.add(source);
    expected.add(target);
    op = new MergeEnrichmentOperator();
    op.initDegrees(2,1);
    op.initPluginId(ResourceFactory.createResource("urn:ex/test/merge-test"));
  }

  @Test
  public void safeApply() {
    Model actual = op.apply(List.of(source, target)).get(0);
    assertTrue("It should merge the models", expected.isIsomorphicWith(actual));
  }

  @Test
  public void predictApplicability() {
    assertEquals("For a perfect merging task applicability should be 1.0", 1.0, op.predictApplicability(List.of(source, target), expected), 0);
    expected.add(expected.createResource(expected.expandPrefix("ex:subject1")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject3")));
    expected.add(expected.createResource(expected.expandPrefix("ex:subject2")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject4")));
    assertEquals("For a flawed merging task with two linking statements and eight distinct statements applicability should be 0.9", 0.9, op.predictApplicability(List.of(source, target), expected), 0.1);
  }

  @Test
  public void reverseApply() {
    expected.add(expected.createResource(expected.expandPrefix("ex:subject1")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject3")));
    expected.add(expected.createResource(expected.expandPrefix("ex:subject2")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject4")));
    List<Model> reconstructed = op.reverseApply(List.of(source, target), expected);
    source.add(expected.createResource(expected.expandPrefix("ex:subject1")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject3")));
    source.add(expected.createResource(expected.expandPrefix("ex:subject2")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject4")));
    target.add(expected.createResource(expected.expandPrefix("ex:subject1")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject3")));
    target.add(expected.createResource(expected.expandPrefix("ex:subject2")), OWL.sameAs, expected.createResource(expected.expandPrefix("ex:subject4")));
    assertTrue("The source dataset is successfully reconstructed", source.isIsomorphicWith(reconstructed.get(0)));
    assertTrue("The target dataset is successfully reconstructed", target.isIsomorphicWith(reconstructed.get(1)));

  }

}