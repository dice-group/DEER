
package org.aksw.deer.enrichments;

import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
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
public class FilterEnrichmentOperator extends AbstractParameterizedEnrichmentOperator implements ReverseLearnable, SelfConfigurable {

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
          Resource o = selectorResource.getPropertyResourceValue(OBJECT);
          logger.info("Running filter " + getId() + " for triple pattern {} {} {} ...",
            s == null ? "[]" : "<" + s.asResource().getURI() + ">",
            p == null ? "[]" : "<" + p.asResource().getURI() + ">",
            o == null ? "[]" : "(<)(\")" + o.toString() + "(\")(>)");
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

  @Override
  public double predictApplicability(List<Model> inputs, Model target) {
    // size of target < input && combined recall of input/target is high.
    Model in = inputs.get(0);
    double propertyIntersectionSize = target.listStatements()
      .mapWith(Statement::getPredicate)
      .filterKeep(p -> in.contains(null, p)).toList().size();
    double stmtIntersectionSize = target.listStatements()
      .filterKeep(in::contains).toList().size();
    double propertyRecall = propertyIntersectionSize/target.size();
    double stmtRecall = stmtIntersectionSize/target.size();
    return stmtRecall * 0.6 + propertyRecall * 0.3 + (in.size()-target.size())/(double)in.size() * 0.1;
  }

  @Override
  public List<Model> reverseApply(List<Model> inputs, Model target) {
    return List.of(ModelFactory.createDefaultModel().add(target).add(inputs.get(0)));
  }

  @Override
  public ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target, ValidatableParameterMap prototype) {
    ValidatableParameterMap result = createParameterMap();
    Model in = inputs.get(0);
    target.listStatements()
      .mapWith(Statement::getPredicate)
      .filterKeep(p -> in.contains(null, p))
      .toSet()
      .forEach(p -> {
        result.add(SELECTOR, result.createResource()
          .addProperty(PREDICATE, p));
      });
    return result.init();
  }

  @Override
  public DegreeBounds getLearnableDegreeBounds() {
    return getDegreeBounds();
  }

}
