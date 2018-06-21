package org.aksw.deer.enrichments;

import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.parameter.conversions.DictListParameterConversion;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.parameter.conversions.StringParameterConversion;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.SimpleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.*;

/**
 */
@Extension
public class FilterEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(FilterEnrichmentOperator.class);

  private static final Property SUBJECT = Vocabulary.property("subject");
  private static final Property PREDICATE = Vocabulary.property("predicate");
  private static final Property OBJECT = Vocabulary.property("object");

  private static final Parameter SELECTORS = new ParameterImpl(
    "selectors",
    new DictListParameterConversion(SUBJECT, PREDICATE, OBJECT), false
  );

  private static final Parameter SPARQL_CONSTRUCT_QUERY = new ParameterImpl(
    "sparqlConstructQuery", StringParameterConversion.getInstance(), false);

  private List<Map<Property, RDFNode>> selectors = new ArrayList<>();
  private String sparqlQuery;

  public FilterEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    return List.of(filterModel(models.get(0)));
  }

  private Model filterModel(Model model) {
    Model resultModel = ModelFactory.createDefaultModel();
    if (sparqlQuery != null) {
      resultModel = QueryExecutionFactory.create(sparqlQuery, model).execConstruct();
    } else {
      for (Map<Property, RDFNode> selectorMap : selectors) {
        RDFNode s = selectorMap.get(SUBJECT);
        RDFNode p = selectorMap.get(PREDICATE);
        SimpleSelector selector = new SimpleSelector(
          s == null ? null : s.asResource(),
          p == null ? null : p.as(Property.class),
          selectorMap.get(OBJECT)
        );
        resultModel.add(model.listStatements(selector));
      }
    }
    return resultModel;
  }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(SELECTORS, SPARQL_CONSTRUCT_QUERY);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    selectors = params.getValue(SELECTORS, new ArrayList<>());
    sparqlQuery = params.getValue(SPARQL_CONSTRUCT_QUERY, null);
    if (selectors.size() == 0) {
      // empty HashMap will select everything - equivalent to "?s ?p ?o"
      selectors.add(new HashMap<>());
    }
  }
  @NotNull
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap result = createParameterMap();
    Model intersection = source.intersection(target);
    if (intersection.isEmpty()) {
      return result;
    }
    List<Map<Property, RDFNode>> selectors = new ArrayList<>();
    intersection.listStatements().forEachRemaining(stmt -> {
      Map<Property, RDFNode> selectorMap = new HashMap<>();
      selectorMap.put(PREDICATE, stmt.getPredicate());
      selectors.add(selectorMap);
    });
    result.setValue(SELECTORS, selectors);
    return result;
  }

}
