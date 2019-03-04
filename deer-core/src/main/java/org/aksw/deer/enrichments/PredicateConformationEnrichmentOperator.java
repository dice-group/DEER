package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Extension
public class PredicateConformationEnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final Property SOURCE_PREDICATE = DEER.property("sourcePredicate");
  public static final Property TARGET_PREDICATE = DEER.property("targetPredicate");
  public static final Property OPERATION = DEER.property("operation");

//  private static final Parameter PROPERTY_MAPPING = new DeerParameter("propertyMapping",
//    new DictListParameterConversion(SOURCE, TARGET), true);

//  private List<Map<Property , RDFNode>> propertyMapping = new ArrayList<>();

//  @NotNull
//  @Override
//  public ParameterMap selfConfig(Model source, Model target) {
//    //@todo improve time complexity to be sub-quadratic
//    ParameterMap result = createParameterMap();
//    List<Map<Property , RDFNode>> propertyDictList = new ArrayList<>();
//    source.listStatements().forEachRemaining(s -> {
//      StmtIterator targetIt = target.listStatements(s.getSubject(), null, s.getObject());
//      if (targetIt.hasNext()) {
//        Statement t = targetIt.next();
//        Map<Property, RDFNode> nodeMap = new HashMap<>();
//        // could be improved by transforming the map to a multiset of POJOs. then, keep X percentile of the multiset
//        if (Objects.equals(s.getSubject(),t.getSubject())) {
//          nodeMap.put(SOURCE, s.getPredicate().asResource());
//          nodeMap.put(TARGET, t.getPredicate().asResource());
//          propertyDictList.add(nodeMap);
//        }
//      }
//    });
//    result.setValue(PROPERTY_MAPPING, propertyDictList);
//    return result;
//  }

  @NotNull
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

}
