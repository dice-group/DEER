package org.aksw.deer.learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aksw.deer.vocabulary.SPECS;
import org.aksw.deer.io.ModelReader;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

/**
 * @author sherif
 */
public class ConfigAnalyzer {

  /**
   * @return all enrichment n the input configModel
   * @author sherif
   */
  public static Set<Resource> getModules(Model configModel) {
    Set<Resource> result = new HashSet<Resource>();
    String sparqlQueryString =
      "SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + SPECS.Module + "> . }";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      Resource module = qs.getResource("?m");
      result.add(module);
    }
    qexec.close();
    return result;
  }

  /**
   * @return all datasets n the input configModel
   * @author sherif
   */
  public static Set<Resource> getDatasets(Model configModel) {
    Set<Resource> result = new HashSet<Resource>();
    String sparqlQueryString =
      "SELECT DISTINCT ?d {?d <" + RDF.type + "> <" + SPECS.Dataset + "> . }";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      Resource module = qs.getResource("?d");
      result.add(module);
    }
    qexec.close();
    return result;
  }

  /**
   * @return The total number of enrichment and operator included in the configModel
   * @author sherif
   */
  public static long size(Model configModel) {
    long count = 0;
    String sparqlQueryString =
      "SELECT (COUNT(DISTINCT ?m) AS ?c) " +
        "{{?m <" + RDF.type + "> <" + SPECS.Module + "> . } " +
        "UNION" +
        "{?m <" + RDF.type + "> <" + SPECS.Operator + "> . }}";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      String s = qs.getLiteral("?c").toString();
      count = Long.parseLong(s.substring(0, s.indexOf("^^")));
    }
    qexec.close();
    return count;
  }

  public static Resource getLastModuleUriOftype(Resource type, Model configModel) {
    List<String> results = new ArrayList<String>();
    String sparqlQueryString =
      "SELECT DISTINCT ?m {?m <" + RDF.type + "> <" + type.getURI() + "> . }";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      Resource module = qs.getResource("?m");
      results.add(module.getURI());
    }
    qexec.close();
    Collections.sort(results);
    return ResourceFactory.createResource(results.get(results.size() - 1));
  }

  public static void main(String args[]) {
    Model m = (new ModelReader()).readModel(args[0]);
    Set<Resource> modules = getModules(m);
    System.out.println("enrichment: " + modules);
    System.out.println("enrichment.size(): " + modules.size());
    System.out.println("size(): " + size(m));

  }

  /**
   * @return The total number of  operator included in the configModel
   * @author sherif
   */
  public static Set<Resource> getOperators(Model configModel) {
    Set<Resource> result = new HashSet<Resource>();
    String sparqlQueryString =
      "SELECT DISTINCT ?o {?o <" + RDF.type + "> <" + SPECS.Operator + "> . }";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      Resource module = qs.getResource("?o");
      result.add(module);
    }
    qexec.close();
    return result;
  }

  /**
   * @author sherif
   */
  public static List<Resource> getFinalDatasets(Model configModel) {
    List<Resource> result = new ArrayList<Resource>();
    String sparqlQueryString =
      "SELECT DISTINCT ?d {?s1 <" + SPECS.hasOutput + "> ?d. " +
        "FILTER (NOT EXISTS {?s2 <" + SPECS.hasInput + "> ?d . } )}";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      Resource dataset = qs.getResource("?d");
      result.add(dataset);
    }
    qexec.close();
    return result;
  }

}
