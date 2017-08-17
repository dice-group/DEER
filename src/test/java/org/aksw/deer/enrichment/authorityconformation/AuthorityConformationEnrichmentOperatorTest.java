package org.aksw.deer.enrichment.authorityconformation;

import org.aksw.deer.io.ModelReader;
import org.aksw.deer.parameter.JenaBackedParameterMap;
import org.aksw.deer.util.EnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kevin Dreßler
 */
public class AuthorityConformationEnrichmentOperatorTest {

  private EnrichmentOperator op;
  private JenaBackedParameterMap map;
  private Model model;

  @Before
  public void setUp() throws Exception {
    op = new AuthorityConformationEnrichmentOperator();
    map = (JenaBackedParameterMap) op.createParameterMap();
    map.setValue(AuthorityConformationEnrichmentOperator.SOURCE_SUBJECT_AUTHORITY, "http://dbpedia.org");
    map.setValue(AuthorityConformationEnrichmentOperator.TARGET_SUBJECT_AUTHORITY, "http://deer.org");
    map.init();
    String sparqlQueryString = "DESCRIBE <http://dbpedia.org/resource/Berlin>";
    QueryEngineHTTP qExec = new QueryEngineHTTP("http://dbpedia.org/sparql", sparqlQueryString);
    qExec.setModelContentType(WebContent.contentTypeJSONLD);
    model = qExec.execDescribe();
    qExec.close();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void init() throws Exception {
    op.init(map, 1, 1);
  }

  @Test
  public void apply() throws Exception {
    op.init(map, 1, 1);
    op.apply(model);
    System.out.println("Everything Completed");
  }

}