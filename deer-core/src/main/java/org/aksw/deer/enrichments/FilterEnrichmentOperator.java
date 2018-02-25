package org.aksw.deer.enrichments;

import org.aksw.faraday_cage.parameter.conversions.DictListParameterConversion;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.SimpleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Extension
public class FilterEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(FilterEnrichmentOperator.class);

  private static final Property SUBJECT = DEER.property("subject");
  private static final Property PREDICATE = DEER.property("predicate");
  private static final Property OBJECT = DEER.property("object");

  private static final Parameter SELECTORS = new ParameterImpl(
    "selectors",
    new DictListParameterConversion(SUBJECT, PREDICATE, OBJECT), false
  );

  private List<Map<Property, RDFNode>> selectors = new ArrayList<>();

  public FilterEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    return List.of(filterModel(models.get(0)));
  }

  private Model filterModel(Model model) {
    Model resultModel = ModelFactory.createDefaultModel();
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
    return resultModel;
  }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(SELECTORS);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    selectors = params.getValue(SELECTORS);
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
