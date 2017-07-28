package org.aksw.deer.enrichment.filter;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
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
public class FilterEnrichmentOperator extends AEnrichmentOperator {

  public static final String TRIPLES_PATTERN = "triplesPattern";
  public static final String TRIPLES_PATTERN_DESC =
    "Set of triple pattern to run against the input model of the filter enrichment. " +
      "By default, this parameter is set to ?s ?p ?o. which generates the whole " +
      "input model as output, changing the values of " +
      "?s, ?p and/or ?o will restrict the output model";
  private static final Logger logger = Logger.getLogger(FilterEnrichmentOperator.class.getName());
  private Model model = ModelFactory.createDefaultModel();
  private String triplesPattern = "?s ?p ?o .";

  public FilterEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> process() {
    this.model = models.get(0);
    if (parameters.containsKey(TRIPLES_PATTERN)) {
      triplesPattern = parameters.get(TRIPLES_PATTERN);
    }
    return Lists.newArrayList(filterModel());
  }

  private Model filterModel() {
    Model resultModel = ModelFactory.createDefaultModel();
    List<Property> accepted = new ArrayList<>();
    if (triplesPattern.contains(" ")) {
      for (String str : triplesPattern.split(" ")) {
        accepted.add(ResourceFactory.createProperty(str));
      }
    } else { // if only one property
      accepted.add(ResourceFactory.createProperty(triplesPattern));
    }
    StmtIterator listStatements = model.listStatements();
    while (listStatements.hasNext()) {
      Statement stat = listStatements.next();
      if (accepted.contains(stat.getPredicate())) {
        resultModel.add(stat);
      }
    }
    return resultModel;
  }

  @Override
  public List<String> getParameters() {
    List<String> parameters = new ArrayList<>();
    parameters.add(TRIPLES_PATTERN);
    return parameters;
  }

  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<>();
    parameters.add(TRIPLES_PATTERN);
    return parameters;
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1,1,1,1);
  }

  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    Map<String, String> parameters = new HashMap<>();
    Model intersection = source.intersection(target);
    if (intersection.isEmpty()) {
      return null;
    }
    triplesPattern = "";
    StmtIterator listStatements = intersection.listStatements();
    while (listStatements.hasNext()) {
      Statement stmnt = listStatements.next();
      triplesPattern += stmnt.getPredicate() + " ";
    }
    parameters.put(TRIPLES_PATTERN, triplesPattern);
    return parameters;
  }

  @Override
  public Resource getType() {
    return SPECS.FilterModule;
  }

}
