package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 
 */
@Extension
public class AuthorityConformationEnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final Property SOURCE_AUTHORITY = DEER.property("sourceAuthority");

  public static final Property TARGET_AUTHORITY = DEER.property("targetAuthority");

  public static final Property OPERATION = DEER.property("operation");

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(OPERATION)
      .declareValidationShape(getValidationModelFor(AuthorityConformationEnrichmentOperator.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    final Model model = models.get(0);
    final Model conformModel = ModelFactory.createDefaultModel();
    final Map<String, String> authorityMapping = new HashMap<>();
    getParameterMap().listPropertyObjects(OPERATION)
      .map(RDFNode::asResource)
      .forEach(op -> {
        final String source = op.getPropertyResourceValue(SOURCE_AUTHORITY).asResource().getURI();
        final String target = op.getPropertyResourceValue(TARGET_AUTHORITY).asResource().getURI();
        authorityMapping.put(source, target);
      });
    model.listStatements().forEachRemaining(stmt -> {
      Resource subject = stmt.getSubject();
      for (String source : authorityMapping.keySet()) {
        if (!Objects.equals(source, "") && subject.getURI().startsWith(source)) {
          String conformedUri = subject.getURI().replaceFirst(source, authorityMapping.get(source));
          subject = ResourceFactory.createResource(conformedUri);
          break;
        }
      }
      conformModel.add(subject, stmt.getPredicate(), stmt.getObject());
    });
    return Lists.newArrayList(conformModel);
  }

//
//  /**
//   * @return Most redundant source URI in the input model
//   */
//  private String getMostRedundantUri(Model m) {
//    Multiset<Resource> subjectsMultiset = HashMultiset.create();
//    ResIterator listSubjects = m.listSubjects();
//    while (listSubjects.hasNext()) {
//      String authority = listSubjects.next().toString();
//      if (authority.contains("#")) {
//        authority = authority.substring(0, authority.indexOf("#"));
//      } else {
//        authority = authority.substring(0, authority.lastIndexOf("/"));
//      }
//      subjectsMultiset.add(ResourceFactory.createResource(authority));
//    }
//    String result = "";
//    int max = 0;
//    for (Resource r : subjectsMultiset) {
//      int i = subjectsMultiset.count(r);
//      if (i > max) {
//        max = i;
//        result = r.getURI();
//      }
//    }
//    return result;
//  }
//
//  /**
//   * Self configuration
//   * Find source/target URI as the most redundant URIs
//   *
//   * @return Map of (key, value) pairs of self configured parameters
//   */
//  //  @Override
//  public ParameterMap selfConfig(Model source, Model target) {
//    ParameterMap parameters = createParameterMap();
//    String s = getMostRedundantUri(source);
//    String t = getMostRedundantUri(target);
//    if (!Objects.equals(s, t)) {
//      parameters.setValue(SOURCE_AUTHORITY, s);
//      parameters.setValue(TARGET_AUTHORITY, s);
//    }
//    return parameters;
//  }

}
