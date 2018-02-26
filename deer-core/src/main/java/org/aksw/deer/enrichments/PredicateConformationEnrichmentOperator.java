package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.parameter.*;
import org.aksw.faraday_cage.parameter.conversions.DictListParameterConversion;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.*;
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
public class PredicateConformationEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  private static final Property SOURCE = Vocabulary.property("source");
  private static final Property TARGET = Vocabulary.property("target");

  private static final Parameter PROPERTY_MAPPING = new ParameterImpl("propertyMapping",
    new DictListParameterConversion(SOURCE, TARGET), true);

  private List<Map<Property , RDFNode>> propertyMapping = new ArrayList<>();

  public PredicateConformationEnrichmentOperator() {
    super();
  }

  @NotNull
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap result = createParameterMap();
    List<Map<Property , RDFNode>> propertyDictList = new ArrayList<>();
    source.listStatements().forEachRemaining(s -> {
      StmtIterator targetIt = target.listStatements(s.getSubject(), null, s.getObject());
      if (targetIt.hasNext()) {
        Map<Property, RDFNode> nodeMap = new HashMap<>();
        nodeMap.put(SOURCE, s.getPredicate().asResource());
        nodeMap.put(TARGET, targetIt.next().getPredicate().asResource());
        propertyDictList.add(nodeMap);
      }
    });
    result.setValue(PROPERTY_MAPPING, propertyDictList);
    return result;
  }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(PROPERTY_MAPPING);
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    Model model = models.get(0);
    //Conform Model
    Model conformModel = ModelFactory.createDefaultModel();
    StmtIterator statmentsIter = model.listStatements();
    while (statmentsIter.hasNext()) {
      Statement statment = statmentsIter.nextStatement();
      Resource s = statment.getSubject();
      Property p = statment.getPredicate();
      RDFNode o = statment.getObject();
      // conform properties
      Property replacement = findReplacement(p);
      conformModel.add(s, replacement != null ? replacement : p, o);
    }
    model = conformModel;
    return Lists.newArrayList(model);
  }

  private Property findReplacement(Property p) {
    final Property[] result = {null};
    propertyMapping.forEach(nodeMap -> {
      if (nodeMap.get(SOURCE).equals(p)) {
        result[0] = nodeMap.get(TARGET).as(Property.class);
      }
    });
    return result[0];
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap parameterMap) {
    this.propertyMapping = parameterMap.getValue(PROPERTY_MAPPING);
  }

}
