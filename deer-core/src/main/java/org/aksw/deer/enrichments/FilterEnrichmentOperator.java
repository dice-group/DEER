
package org.aksw.deer.enrichments;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 */
@Extension
public class FilterEnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(FilterEnrichmentOperator.class);

  public static final Property SUBJECT = DEER.property("subject");
  public static final Property PREDICATE = DEER.property("predicate");
  public static final Property OBJECT = DEER.property("object");
  public static final Property SELECTOR = DEER.property("selector");
  public static final Property SPARQL_CONSTRUCT_QUERY = DEER.property("sparqlConstructQuery");

  public FilterEnrichmentOperator() {
    super();
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(SELECTOR)
      .declareProperty(SPARQL_CONSTRUCT_QUERY)
      .declareValidationShape(getValidationModelFor(FilterEnrichmentOperator.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    return List.of(filterModel(models.get(0)));
  }

  private Model filterModel(Model model) {
    final Model resultModel = ModelFactory.createDefaultModel();
    final Optional<RDFNode> sparqlQuery = getParameterMap()
      .getOptional(SPARQL_CONSTRUCT_QUERY);
    if (sparqlQuery.isPresent()) {
      logger.info("Executing SPARQL CONSTRUCT query for " + getId() + " ...");
      return QueryExecutionFactory
        .create(sparqlQuery.get().asLiteral().getString(), model)
        .execConstruct();
    } else {
      getParameterMap().listPropertyObjects(SELECTOR)
        .map(RDFNode::asResource)
        .forEach(selectorResource -> {
        RDFNode s = selectorResource.getPropertyResourceValue(SUBJECT);
        RDFNode p = selectorResource.getPropertyResourceValue(PREDICATE);
        Statement o = selectorResource.getProperty(OBJECT);
        logger.info("Filtering " + getId() + " for triple pattern {} {} {} ...",
          s == null ? "[]" : "<" + s.asResource().getURI() + ">",
          p == null ? "[]" : "<" + p.asResource().getURI() + ">",
          o == null ? "[]" : "(<)(\")" + o.getObject().toString() + "(\")(>)");
        SimpleSelector selector = new SimpleSelector(
          s == null ? null : s.asResource(),
          p == null ? null : p.as(Property.class),
          o
        );
        resultModel.add(model.listStatements(selector));
      });
    }
    return resultModel;
  }

//  //  @Override
//  public ParameterMap selfConfig(Model source, Model target) {
//    ParameterMap result = createParameterMap();
//    Model intersection = source.intersection(target);
//    if (intersection.isEmpty()) {
//      return result;
//    }
//    List<Map<Property, RDFNode>> selectors = new ArrayList<>();
//    intersection.listStatements().forEachRemaining(stmt -> {
//      Map<Property, RDFNode> selectorMap = new HashMap<>();
//      selectorMap.put(PREDICATE, stmt.getPredicate());
//      selectors.add(selectorMap);
//    });
//    result.setValue(SELECTORS, selectors);
//    return result;
//  }



//
//  private static final Parameter SELECTORS = new DeerParameter(
//    "selectors",
//    new DictListParameterConversion(SUBJECT, PREDICATE, OBJECT), false
//  );
//
//  private static final Parameter SPARQL_CONSTRUCT_QUERY = new DeerParameter(
//    "sparqlConstructQuery", StringParameterConversion.getInstance(), false);


}
