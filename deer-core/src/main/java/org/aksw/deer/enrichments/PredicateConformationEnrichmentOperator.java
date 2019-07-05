package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 */
@Extension
public class PredicateConformationEnrichmentOperator extends AbstractParameterizedEnrichmentOperator implements ReverseLearnable, SelfConfigurable {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final Property SOURCE_PREDICATE = DEER.property("sourcePredicate");
  public static final Property TARGET_PREDICATE = DEER.property("targetPredicate");
  public static final Property OPERATION = DEER.property("operation");

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(OPERATION)
      .declareValidationShape(getValidationModelFor(PredicateConformationEnrichmentOperator.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    final Model model = models.get(0);
    final Model conformModel = ModelFactory.createDefaultModel();
    final Map<Property, Property> propertyMapping = new HashMap<>();
    getParameterMap().listPropertyObjects(OPERATION)
      .map(RDFNode::asResource)
      .forEach(op -> {
        final Property source = op.getPropertyResourceValue(SOURCE_PREDICATE).as(Property.class);
        final Property target = op.getPropertyResourceValue(TARGET_PREDICATE).as(Property.class);
        propertyMapping.put(source, target);
      });
    model.listStatements().forEachRemaining(stmt -> {
      Property p = stmt.getPredicate();
      // conform properties
      if (propertyMapping.containsKey(p)) {
        p = propertyMapping.get(p);
      }
      conformModel.add(stmt.getSubject(), p, stmt.getObject());
    });
    return Lists.newArrayList(conformModel);
  }

  @Override
  public double predictApplicability(List<Model> inputs, Model target) {
    return learnParameterMap(inputs, target, null).listPropertyObjects(OPERATION).count() > 0 ? 1 : 0;
  }

  @SuppressWarnings("Duplicates")
  @Override
  public List<Model> reverseApply(List<Model> inputs, Model target) {
    ValidatableParameterMap reverseParameterMap = createParameterMap();
    learnParameterMap(inputs, target, null).listPropertyObjects(OPERATION)
      .forEach(r -> reverseParameterMap.add(OPERATION, reverseParameterMap.createResource()
        .addProperty(SOURCE_PREDICATE, r.asResource().getPropertyResourceValue(TARGET_PREDICATE))
        .addProperty(TARGET_PREDICATE, r.asResource().getPropertyResourceValue(SOURCE_PREDICATE))
      ));
    initParameters(reverseParameterMap.init());
    return safeApply(List.of(target));
  }

  @Override
  public ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target, ValidatableParameterMap prototype) {
    ValidatableParameterMap result = createParameterMap();
    Model source = inputs.get(0);
    Set<Property> seen = new HashSet<>();
    source.listStatements().forEachRemaining(s -> {
      if (seen.contains(s.getPredicate())) {
        return;
      }
      seen.add(s.getPredicate());
      target.listStatements(s.getSubject(), null, s.getObject())
        .nextOptional()
        .ifPresent(t -> {
          if (!Objects.equals(s.getPredicate(), t.getPredicate())) {
            result.add(OPERATION, result.createResource()
              .addProperty(SOURCE_PREDICATE, s.getPredicate())
              .addProperty(TARGET_PREDICATE, t.getPredicate())
            );
          }
        });
    });
    return result.init();
  }

  @Override
  public DegreeBounds getLearnableDegreeBounds() {
    return getDegreeBounds();
  }

}
