package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.*;
import org.aksw.deer.learning.Learnable;
import org.aksw.deer.learning.RandomUtil;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DEERA;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.PluginFactory;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.Resource;
import org.pf4j.DefaultPluginManager;

import java.util.*;

/**
 *
 */
public class RandomOperatorFactory {


  private static PluginFactory<EnrichmentOperator> factory = new PluginFactory<>(EnrichmentOperator.class, new DefaultPluginManager(), FCAGE.ExecutionNode);

  private static Set<Resource> allowedTypes = new HashSet<>(List.of(
    new LinkingEnrichmentOperator().getType(),
    new NEREnrichmentOperator().getType(),
    new PredicateConformationEnrichmentOperator().getType(),
    new AuthorityConformationEnrichmentOperator().getType(),
    new FilterEnrichmentOperator().getType(),
    new MergeEnrichmentOperator().getType(),
    new DereferencingEnrichmentOperator().getType()));

  private static List<Resource> availableOps;

  private static int maxArity = 0;

  private static Map<Integer, List<Resource>> arityOpsMap;

  private RandomOperatorFactory() { }

  private static void setup() {
    arityOpsMap = new HashMap<>();
    availableOps = new ArrayList<>();
    factory.listAvailable().stream()
      .filter(allowedTypes::contains)
      .map(factory::getImplementationOf)
      .filter(op -> op instanceof Learnable && (!(op instanceof Parameterized) || (op instanceof SelfConfigurable)))
      .filter(op -> ((Learnable) op).getLearnableDegreeBounds().minOut() == 1)
      .forEach(op -> {
        availableOps.add(op.getType());
        ExecutionNode.DegreeBounds degreeBounds = ((Learnable) op).getLearnableDegreeBounds();
        for (int i = degreeBounds.minIn(); i <= degreeBounds.maxIn(); i++) {
          if (i > maxArity) {
            maxArity = i;
          }
          if (!arityOpsMap.containsKey(i)) {
            arityOpsMap.put(i, new ArrayList<>());
          }
          arityOpsMap.get(i).add(op.getType());
        }
      });
  }

  static { setup(); }

  public static void setAllowedTypes(Set<Resource> allowedTypes) {
    RandomOperatorFactory.allowedTypes = allowedTypes;
    setup();
  }

  public static void setFactory(PluginFactory<EnrichmentOperator> factory) {
    RandomOperatorFactory.factory = factory;
    setup();
  }

  public static EnrichmentOperator getForMaxArity(int arity) {
    if (!arityOpsMap.containsKey(arity)) {
      throw new IllegalArgumentException("There are no available operators with in degree = " + arity + " and out degree = 1");
    }
    EnrichmentOperator op;
    do {
      op = factory.getImplementationOf(availableOps.get(RandomUtil.get(availableOps.size())));
    } while (arity < ((Learnable) op).getLearnableDegreeBounds().minIn());
    op.initPluginId(DEERA.forExecutionNode(op));
    return op;
  }

  public static EnrichmentOperator getForArity(int arity) {
    if (!arityOpsMap.containsKey(arity)) {
      throw new IllegalArgumentException("There are no available operators with in degree = " + arity + " and out degree = 1");
    }
    List<Resource> bucket = arityOpsMap.get(arity);
    EnrichmentOperator op = factory.getImplementationOf(bucket.get(RandomUtil.get(bucket.size())));
    op.initPluginId(DEERA.forExecutionNode(op));
    return op;
  }

  public static EnrichmentOperator reproduce(EnrichmentOperator op) {
    EnrichmentOperator clone = factory.getImplementationOf(op.getType());
    clone.initPluginId(op.getId());
    return clone;
  }

  public static int getMaxArity() {
    return maxArity;
  }


}
