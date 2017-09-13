package org.aksw.deer.enrichment.predicateconformation;

import com.google.common.collect.Lists;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.enrichment.authorityconformation.AuthorityConformationEnrichmentOperator;
import org.aksw.deer.parameter.DefaultParameter;
import org.aksw.deer.parameter.DefaultParameterMap;
import org.aksw.deer.parameter.Parameter;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sherif
 */
@Extension
public class PredicateConformationEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final Parameter PROPERTY_MAP = new DefaultParameter(
    "propertyMap",
    "List of (source, target) pairs of property URIs. For each pair," +
      "source in input model will be conformed to target.",
    PropertyMapParameterConversion.INSTANCE, true);

  private Map<Property, Property> propertyMap = new HashMap<>();

  public PredicateConformationEnrichmentOperator() {
    super();
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,1,1,1);
  }

  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap result = createParameterMap();
    Map<Property, Property> propertyMap = new HashMap<>();
    source.listStatements().forEachRemaining(s -> {
      StmtIterator stmtIterator = target.listStatements(s.getSubject(), null, s.getObject());
      if (stmtIterator.hasNext()) {
        propertyMap.put(s.getPredicate(), stmtIterator.next().getPredicate());
      }
    });
    result.setValue(PROPERTY_MAP, propertyMap);
    return result;
  }

  @Override
  public ParameterMap createParameterMap() {
    return new DefaultParameterMap(PROPERTY_MAP);
  }

  @Override
  protected List<Model> process() {
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
      if (propertyMap.containsKey(p)) {
        p = propertyMap.get(p);
      }
      conformModel.add(s, p, o);
    }
    model = conformModel;
    return Lists.newArrayList(model);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void accept(ParameterMap parameterMap) {
    this.propertyMap = parameterMap.getValue(PROPERTY_MAP);
  }

}