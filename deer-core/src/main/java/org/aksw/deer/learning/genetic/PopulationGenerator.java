package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.PluginFactory;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.Resource;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class PopulationGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PopulationGenerator.class);

  private PluginFactory<EnrichmentOperator> factory;

  private List<Resource> availableOps;

  PopulationGenerator(PluginManager pluginManager, Set<Resource> types) {
    factory = new PluginFactory<>(EnrichmentOperator.class, pluginManager, FCAGE.ExecutionNode);
    availableOps = factory.listAvailable().stream().filter(types::contains).collect(Collectors.toList());
  }

  public static void main(String[] args) {
    Set<Resource> types = new HashSet<>();
    types.add(DEER.resource("FilterEnrichmentOperator"));
    types.add(DEER.resource("LinkingEnrichmentOperator"));
    types.add(DEER.resource("NEREnrichmentOperator"));
    types.add(DEER.resource("DereferencingEnrichmentOperator"));
    types.add(DEER.resource("AuthorityConformationEnrichmentOperator"));
    types.add(DEER.resource("PredicateConformationEnrichmentOperator"));
    types.add(DEER.resource("GeoDistanceEnrichmentOperator"));
    new PopulationGenerator(new DefaultPluginManager(), types);
  }

//  private ExecutionGraph<DeerExecutionNode> generateOne() {
//    List<DeerExecutionNode>
//    RandomUtil.get();
//
//  }



}