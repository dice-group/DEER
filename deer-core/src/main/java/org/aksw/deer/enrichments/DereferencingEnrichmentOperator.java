package org.aksw.deer.enrichments;

import com.github.therapi.runtimejavadoc.RetainJavadoc;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.faraday_cage.parameter.conversions.DictListParameterConversion;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * An EnrichmentOperator for dereferencing.
 * <p>
 * Dereferencing is a method of expanding knowledge of resources that belong to an
 * external knowledge base.
 * We define a dereferencing operation as the following sequence of operations:
 * <ol>
 *     <li>Query the local model for starting resources {@code ?x}
 *         that belong to an external knowledge base</li>
 *     <li>Query the external knowledge base for triples {@code (?x, d, ?y)},
 *         where d is a defined property path in the external knowledge base.</li>
 *     <li>Add all results to the local model in the form of {@code (?x, i, ?y)},
 *         where i is the property name under which they should be imported.</li>
 * </ol>
 * <h2>Configuration</h2>
 *
 * <h3>{@code :operations}</h3>
 *
 * A {@link DictListParameterConversion DictList}.
 *
 * Each entry in the {@code DictList} corresponds to one dereferencing operation, allowing multiple
 * dereferencing operations being carried out by a single {@code DereferencingEnrichmentOperator}.
 * Each entry may contain the following properties:
 *
 * <blockquote>
 *     <b>{@code :lookUpProperty}</b>
 *     <i>range: resource</i>
 *     <br>
 *     Determines the starting resources {@code ?x} as all objects of triples having
 *     the value of {@code :lookUpProperty} as predicate.
 * </blockquote>
 *
 * <blockquote>
 *     <b>{@code :lookUpPrefix} [required]</b>
 *     <i>range: string</i>
 *     <br>
 *     Determines the starting resources {@code ?x} as all objects of triples having
 *     the value of {@code :lookUpProperty} as predicate.
 * </blockquote>
 *
 * <blockquote>
 *     <b>{@code :dereferencingProperty} [required]</b>
 *     <i>range: resource</i>
 *     <br>
 *     Look up the values to be imported to the local model in the external knowledge base
 *     using the property defined by the value of {@code :dereferencingProperty}.
 * </blockquote>
 *
 * <blockquote>
 *     <b>{@code :importProperty} [required]</b>
 *     <i>range: resource</i>
 *     <br>
 *     Add looked up values to starting resources using the value of :importProperty.
 * </blockquote>
 *
 *
 * <h2>Example</h2>
 *
 */
@Extension @RetainJavadoc
public class DereferencingEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

//   * <blockquote>
//   *     <b>{@code :endpoint} </b>
//   *     <i>range: string</i>
//   *     <br>
//   *     URL of the SPARQL endpoint to use for this dereferencing operation.
//   *     If not given, this operator tries to infer the endpoint from the starting resources URIs.
//   * </blockquote>



  private static final Logger logger = LoggerFactory.getLogger(DereferencingEnrichmentOperator.class);

  private static final Property LOOKUP_PROPERTY = Vocabulary.property("lookUpProperty");

  private static final Property LOOKUP_PREFIX = Vocabulary.property("lookUpPrefix");

  private static final Property DEREFERENCING_PROPERTY = Vocabulary.property("dereferencingProperty");

  private static final Property IMPORT_PROPERTY = Vocabulary.property("importProperty");

  private static final Parameter OPERATIONS = new ParameterImpl("operations",
    new DictListParameterConversion(LOOKUP_PREFIX, LOOKUP_PROPERTY, DEREFERENCING_PROPERTY,
      IMPORT_PROPERTY), true);

  private static final String DEFAULT_LOOKUP_PREFIX = "http://dbpedia.org/resource";

  private static final Set<Property> ignoredProperties = new HashSet<>(Arrays.asList(OWL.sameAs));

  private HashMap<OperationGroup, Set<Property[]>> operations;

  private Model model;

  public DereferencingEnrichmentOperator() {
    super();
  }

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
   *
   * @param source source
   * @param target target
   *
   * @return Map of (key, value) pairs of self configured parameters
   */
  @NotNull
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap parameters = createParameterMap();
    Set<Property> propertyDifference = getPropertyDifference(source, target);
    List<Map<Property, RDFNode>> autoOperations = new ArrayList<>();
    for (Property property : propertyDifference) {
      Map<Property, RDFNode> autoOperation = new HashMap<>();
      autoOperation.put(DEREFERENCING_PROPERTY, property);
      autoOperation.put(IMPORT_PROPERTY, property);
      autoOperations.add(autoOperation);
    }
    parameters.setValue(OPERATIONS, autoOperations);
    return parameters;
  }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(OPERATIONS);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    List<Map<Property, RDFNode>> origOps = params.getValue(OPERATIONS);
    this.operations = new HashMap<>();
    for (Map<Property, RDFNode> op : origOps) {
      String lookUpPrefix = op.get(LOOKUP_PREFIX) == null
        ? DEFAULT_LOOKUP_PREFIX : op.get(LOOKUP_PREFIX).toString();
      Property lookUpProperty = op.get(LOOKUP_PROPERTY) == null
        ? null : op.get(LOOKUP_PROPERTY).as(Property.class);
      Property dereferencingProperty = op.get(DEREFERENCING_PROPERTY) == null
        ? null : op.get(DEREFERENCING_PROPERTY).as(Property.class);
      Property importProperty = op.get(IMPORT_PROPERTY) == null
        ? dereferencingProperty : op.get(IMPORT_PROPERTY).as(Property.class);
      OperationGroup opGroup = new OperationGroup(lookUpProperty, lookUpPrefix);
      if (!operations.containsKey(opGroup)) {
        operations.put(opGroup, new HashSet<>());
      }
      operations.get(opGroup).add(new Property[]{dereferencingProperty, importProperty});
    }
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    model = ModelFactory.createDefaultModel().add(models.get(0));
    operations.forEach(this::runOperation);
    return Lists.newArrayList(model);
  }

  private void runOperation(OperationGroup opGroup, Set<Property[]> ops) {
    Map<Resource, List<Pair<Property, RDFNode>>> dereffedPerResource = new HashMap<>();
    List<Statement> candidateNodes = getCandidateNodes(opGroup.lookupPrefix, opGroup.lookupProperty);
    candidateNodes.stream()
      .map(Statement::getResource)
      .distinct()
      .forEach(o -> dereffedPerResource.put(o, getEnrichmentPairsFor(o, ops)));
    for (Statement stmt : candidateNodes) {
      for (Pair<Property, RDFNode> pair : dereffedPerResource.get(stmt.getResource())) {
        stmt.getSubject().addProperty(pair.getLeft(), pair.getRight());
      }
    }
  }

  private List<Pair<Property, RDFNode>> getEnrichmentPairsFor(Resource o, Set<Property[]> ops) {
    List<Pair<Property, RDFNode>> result = new ArrayList<>();
    Model resourceModel = queryResourceModel(o);
    for (Property[] op : ops) {
      resourceModel.listStatements(o, op[0], (RDFNode)null)
        .mapWith(Statement::getObject)
        .forEachRemaining(x -> result.add(new ImmutablePair<>(op[1], x)));
    }
    return result;
  }

  private Model queryResourceModel(Resource o) {
    Model result = ModelFactory.createDefaultModel();
    URL url;
    URLConnection conn = null;
    try {
      url = new URL(o.getURI());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return result;
    }
    try {
      conn = url.openConnection();
      conn.setRequestProperty("Accept", "application/rdf+xml");
      conn.setRequestProperty("Accept-Language", "en");
      return ModelFactory.createDefaultModel()
        .read(conn.getInputStream(), null);
    } catch (ConnectException e) {
      if (e.getMessage().contains("refused")) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private List<Statement> getCandidateNodes(String lookupPrefix, Property lookUpProperty) {
    return model.listStatements()
      .filterKeep(statement -> statement.getObject().isURIResource() &&
        statement.getObject().asResource().getURI().startsWith(lookupPrefix) &&
        (lookUpProperty == null || statement.getPredicate().equals(lookUpProperty)))
      .toList();
  }

  private Set<Property> getPropertyDifference(Model source, Model target) {
    Function<Model, Set<Property>> getProperties =
      (m) -> m.listStatements().mapWith(Statement::getPredicate).toSet();
    return Sets.difference(getProperties.apply(source), getProperties.apply(target))
      .stream().filter(p -> !ignoredProperties.contains(p)).collect(Collectors.toSet());
  }

  private static class OperationGroup {

    private final Property lookupProperty;
    private final String lookupPrefix;

    private OperationGroup(Property lookupProperty, String lookupPrefix) {
      this.lookupProperty = lookupProperty;
      this.lookupPrefix = lookupPrefix;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof  OperationGroup)) {
        return false;
      }
      OperationGroup other = (OperationGroup) obj;
      return (lookupPrefix == null ? other.lookupPrefix == null : lookupPrefix.equals(other.lookupPrefix))
        && (lookupProperty == null ? other.lookupProperty == null : lookupProperty.equals(other.lookupProperty));
    }

    @Override
    public int hashCode() {
      return (lookupPrefix == null ? -1 : lookupPrefix.hashCode()) + 13 * (lookupProperty == null ? -1 : lookupProperty.hashCode());
    }
  }

}
