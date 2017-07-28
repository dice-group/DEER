package org.aksw.deer.enrichment.predicateconformation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author sherif
 */
@Extension
public class PredicateConformationEnrichmentOperator extends AEnrichmentOperator {

  // parameters keys
  public static final String SOURCE_PROPERTY = "sourceProperty";
  public static final String SOURCE_PROPERTY_DESC = "Source property to be replaced by target property";
  public static final String TARGET_PROPERTY = "targetProperty";
  public static final String TARGET_PROPERTY_DESC = "Target property to replace source property";
  private static final Logger logger = Logger.getLogger(PredicateConformationEnrichmentOperator.class.getName());
  // parameters list
  private Map<Property, Property> propertyMap = new HashMap<>();

  public PredicateConformationEnrichmentOperator() {
    super();
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,1,1,1);
  }

  /**
   * Self configuration
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  public Map<String, String> selfConfig(Model source, Model target) {
    Map<String, String> parameters = new HashMap<>();
    long i = 1;
    // commonSubjects = common subjects of source and target
    Set<Resource> sSubjects = new HashSet<>();
    ResIterator subjects = source.listSubjects();
    while (subjects.hasNext()) {
      sSubjects.add(subjects.next());
    }
    Set<Resource> tSubjects = new HashSet<>();
    subjects = source.listSubjects();
    while (subjects.hasNext()) {
      tSubjects.add(subjects.next());
    }
    Set<Resource> commonSubjects = Sets.intersection(sSubjects, tSubjects);
    if (commonSubjects.isEmpty()) {
      return null;
    }
    // commonObjects = for each Subject in commonSubjects find common objects of source and target
    for (Resource s : commonSubjects) {
      StmtIterator statements = source.listStatements(s, null, (RDFNode) null);
      Set<RDFNode> sObjects = new HashSet<>();
      while (statements.hasNext()) {
        sObjects.add(statements.next().getObject());
      }
      statements = target.listStatements(s, null, (RDFNode) null);
      Set<RDFNode> tObjects = new HashSet<>();
      while (statements.hasNext()) {
        tObjects.add(statements.next().getObject());
      }
      Set<RDFNode> commonObjects = Sets.intersection(sObjects, tObjects);
      if (commonObjects.isEmpty()) {
        return null;
      }
      // find different predicate to be conformed
      for (RDFNode o : commonObjects) {
        Property sProperty = null, tProperty = null;
        statements = source.listStatements(s, null, o);
        while (statements.hasNext()) {
          sProperty = statements.next().getPredicate();
        }
        statements = target.listStatements(s, null, o);
        while (statements.hasNext()) {
          tProperty = statements.next().getPredicate();
        }
        if (sProperty != null && tProperty != null &&
          !sProperty.equals(tProperty) &&
          !parameters.containsKey(sProperty.toString()) &&
          !parameters.containsValue(tProperty.toString())) {
          parameters.put(SOURCE_PROPERTY + i, sProperty.toString());
          parameters.put(TARGET_PROPERTY + i, tProperty.toString());
          i++;
        }
      }
    }
    return parameters;
  }

  @Override
  protected List<Model> process() {
    Model model = models.get(0);
    //Read parameters
    boolean parameterFound = false;
    for (long i = 1;
      parameters.containsKey(SOURCE_PROPERTY + i) && parameters.containsKey(TARGET_PROPERTY + i);
      i++) {
      Property inputProperty = ResourceFactory.createProperty(parameters.get(SOURCE_PROPERTY + i));
      Property conformProperty = ResourceFactory
        .createProperty(parameters.get(TARGET_PROPERTY + i));
      propertyMap.put(inputProperty, conformProperty);
      parameterFound = true;
    }
    if (!parameterFound) {
      return Lists.newArrayList(model);
    }

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
  public List<String> getParameters() {
    List<String> parameters = new ArrayList<>();
    parameters.add(SOURCE_PROPERTY + "<i>");
    parameters.add(TARGET_PROPERTY + "<i>");
    return parameters;
  }

  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<>();
    parameters.add(SOURCE_PROPERTY + "<i>");
    parameters.add(TARGET_PROPERTY + "<i>");
    return parameters;
  }


  @Override
  public String getDescription() {
    return null;
  }


}
