package org.aksw.deer.enrichment.filter;

import com.google.common.collect.Lists;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.parameter.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin Dreßler
 */
@Extension
public class FilterEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(FilterEnrichmentOperator.class);

  private static final Parameter SELECTORS = new DefaultParameter(
    "selectors",
    "Set of triple pattern to run against the input model of the filter enrichment. " +
      "By default, this parameter is set to ?s ?p ?o. which generates the whole " +
      "input model as output, changing the values of " +
      "?s, ?p and/or ?o will restrict the output model",
    new DictListParameterConversion(RDF.subject, RDF.predicate, RDF.object), false
  );

  private List<Map<Property, RDFNode>> selectors = new ArrayList<>();

  public FilterEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> process() {
    return Lists.newArrayList(filterModel(models.get(0)));
  }

  private Model filterModel(Model model) {
    Model resultModel = ModelFactory.createDefaultModel();
    for (Map<Property, RDFNode> selectorMap : selectors) {
      RDFNode s = selectorMap.get(RDF.subject);
      RDFNode p = selectorMap.get(RDF.predicate);
      SimpleSelector selector = new SimpleSelector(
        s == null ? null : s.asResource(),
        p == null ? null : p.as(Property.class),
        selectorMap.get(RDF.object)
      );
      resultModel.add(model.listStatements(selector));
    }
    return resultModel;
  }

  @Override
  public ParameterMap createParameterMap() {
    return new DefaultParameterMap(SELECTORS);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void accept(ParameterMap params) {
    selectors = params.getValue(SELECTORS);
    if (selectors.size() == 0) {
      // empty HashMap will select everything - equivalent to "?s ?p ?o"
      selectors.add(new HashMap<>());
    }
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DefaultDegreeBounds(1,1,1,1);
  }

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
      selectorMap.put(RDF.predicate, stmt.getPredicate());
      selectors.add(selectorMap);
    });
    result.setValue(SELECTORS, selectors);
    return result;
  }

}
