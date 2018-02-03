package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 */
public class QueryHelper {

  public static void forEachResultOf(Query q, Model m, Consumer<QuerySolution> f) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      f.accept(qs);
    }
    qExec.close();
  }

  public static void forEachResultOf(String q, Model m, Consumer<QuerySolution> f) {
    forEachResultOf(QueryFactory.create(q), m, f);
  }

  public static <V> List<V> mapResultOf(Query q, Model m, Function<QuerySolution, V> f) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    List<V> result = new ArrayList<>();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      result.add(f.apply(qs));
    }
    qExec.close();
    return result;
  }

  public static <V> List<V> mapResultOf(String q, Model m, Function<QuerySolution, V> f) {
    return mapResultOf(QueryFactory.create(q), m, f);
  }

  public static boolean hasEmptyResult(Query q, Model m) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    return !queryResults.hasNext();
  }

  public static String not(String s) {
    return "NOT " + s;
  }

  public static String exists(String s) {
    return "EXISTS { " + s + " }";
  }

  public static String triple(String s, Property p, Resource o) {
    return s + " <" + p + "> <" + o + "> .";
  }

  public static String triple(String s, Property p, String o) {
    return s + " <" + p + "> " + o + " .";
  }

  public static String triple(Resource s, Property p, Resource o) {
    return "<" + s + "> <" + p + "> <" + o + "> .";
  }

  public static String triple(Resource s, Property p, String o) {
    return "<" + s + "> <" + p + "> " + o + " .";
  }

}
