package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.learning.ReverseLearnable;
import org.aksw.deer.learning.SelfConfigurable;
import org.aksw.deer.vocabulary.DBR;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ThreadlocalInheritingCompletableFuture;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
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
 *
 *
 * Each operation corresponds to one dereferencing operation, allowing multiple
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
@Extension
public class DereferencingEnrichmentOperator extends AbstractParameterizedEnrichmentOperator implements ReverseLearnable, SelfConfigurable {

//   * <blockquote>
//   *     <b>{@code :endpoint} </b>
//   *     <i>range: string</i>
//   *     <br>
//   *     URL of the SPARQL endpoint to use for this dereferencing operation.
//   *     If not given, this operator tries to infer the endpoint from the starting resources URIs.
//   * </blockquote>



  private static final Logger logger = LoggerFactory.getLogger(DereferencingEnrichmentOperator.class);

  public static final Property LOOKUP_PROPERTY = DEER.property("lookUpProperty");

  public static final Property LOOKUP_PREFIX = DEER.property("lookUpPrefix");

  public static final Property DEREFERENCING_PROPERTY = DEER.property("dereferencingProperty");

  public static final Property IMPORT_PROPERTY = DEER.property("importProperty");

  public static final Property OPERATION = DEER.property("operation");

  public static final Property USE_SPARQL_ENDPOINT = DEER.property("useSparqlEndpoint");

  private static String DEFAULT_LOOKUP_PREFIX = DBR.getURI();

  private HashMap<OperationGroup, Set<Property[]>> operations;

  private Optional<String> endpoint;

  private Model model;


  private static final ConcurrentMap<Resource, CompletableFuture<Model>> cache = new ConcurrentHashMap<>();

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(OPERATION)
      .declareProperty(USE_SPARQL_ENDPOINT)
      .declareValidationShape(getValidationModelFor(DereferencingEnrichmentOperator.class))
      .build();
  }


  public void initializeOperations() {
    ValidatableParameterMap parameters = getParameterMap();
    this.operations = new HashMap<>();
    parameters.listPropertyObjects(OPERATION)
      .map(RDFNode::asResource)
      .forEach(op -> {
        String lookUpPrefix = !op.hasProperty(LOOKUP_PREFIX)
          ? DEFAULT_LOOKUP_PREFIX : op.getProperty(LOOKUP_PREFIX).getLiteral().getString();
        Property lookUpProperty = !op.hasProperty(LOOKUP_PROPERTY)
          ? null : op.getPropertyResourceValue(LOOKUP_PROPERTY).as(Property.class);
        Property dereferencingProperty = !op.hasProperty(DEREFERENCING_PROPERTY)
          ? null : op.getPropertyResourceValue(DEREFERENCING_PROPERTY).as(Property.class);
        Property importProperty = !op.hasProperty(IMPORT_PROPERTY)
          ? dereferencingProperty : op.getPropertyResourceValue(IMPORT_PROPERTY).as(Property.class);
        OperationGroup opGroup = new OperationGroup(lookUpProperty, lookUpPrefix);
        if (!operations.containsKey(opGroup)) {
          operations.put(opGroup, new HashSet<>());
        }
        operations.get(opGroup).add(new Property[]{dereferencingProperty, importProperty});
      });
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    endpoint = getParameterMap().getOptional(USE_SPARQL_ENDPOINT)
      .map(r -> r.asResource().getURI());
    initializeOperations();
    model = ModelFactory.createDefaultModel().add(models.get(0));
    operations.forEach(endpoint.isPresent() ? this::runSPARQLOperation : this::runOperation);
    return Lists.newArrayList(model);
  }

  /**
   * Implementation of HTTP-based lookup.
   * Groups HTTP calls per lookup resource, retrieves
   * Model via cached  HTTP calls.
   */
  private void runOperation(OperationGroup opGroup, Set<Property[]> ops) {
    Map<Resource, List<Pair<Property, RDFNode>>> dereffedPerResource = new HashMap<>();
    MultiMap<Resource, Resource> candidateNodes = getCandidateNodeMap(opGroup.lookupPrefix, opGroup.lookupProperty);
    candidateNodes.keySet()
      .forEach(o -> dereffedPerResource.put(o, getEnrichmentPairsFor(o, ops)));
    for (Map.Entry<Resource, Collection<Resource>> entry : candidateNodes.entrySet()) {
      for (Resource subject : entry.getValue()) {
        for (Pair<Property, RDFNode> pair : dereffedPerResource.get(entry.getKey())) {
          subject.addProperty(pair.getLeft(), pair.getRight());
        }
      }
    }
  }


  /**
   * Get SPARQL Endpoint suggestions based on WIMU (will this work for prefixes?)
   *    * If not, need to query for each resource -> can reuse same structure as HTTP-based
   *    lookup...
   *
   *
   */


  /**
   *
   * Implementation of SPARQL lookup.
   * Here we will group by operation to achieve higher throughput and less overhead.
   * Question: Is SELECT or CONSTRUCT better for this? (or DESCRIBE)
   */
  private void runSPARQLOperation(OperationGroup opGroup, Set<Property[]> ops) {
    String queryStart = "SELECT ?source ?import ?dereffed WHERE { VALUES (?source ?deref ?import) { ";
    String queryEnd = "} ?source ?deref ?dereffed . }";
    MultiMap<Resource, Resource> candidateNodes = getCandidateNodeMap(opGroup.lookupPrefix, opGroup.lookupProperty);
    int i = 0;
    int c = 0;
    int j = 0;
    StringBuilder sparqlQueryBuilder = new StringBuilder();
    for (Resource o : candidateNodes.keySet()) {
      i++;
      for (Property[] op : ops) {
        sparqlQueryBuilder.append("(<");
        sparqlQueryBuilder.append(o.asResource().getURI());
        sparqlQueryBuilder.append("> <");
        sparqlQueryBuilder.append(op[0]);
        sparqlQueryBuilder.append("> <");
        sparqlQueryBuilder.append(op[1]);
        sparqlQueryBuilder.append(">)\n");
        if (++c > 1000) {
          j += queryAndAddToModel(queryStart + sparqlQueryBuilder.toString() + queryEnd, candidateNodes);
          c = 0;
          sparqlQueryBuilder = new StringBuilder();
        }
      }
    }
    if (c > 0) {
      queryAndAddToModel(queryStart + sparqlQueryBuilder.toString() + queryEnd, candidateNodes);
    }
//    logger.info("Trying for {} possible dereferencings for lookup {}", i * ops.size(), opGroup);
    logger.info("Dereferenced {} of {} possible properties for lookup {}", j, i * ops.size(), opGroup);
  }

  private int queryAndAddToModel(String query, MultiMap<Resource, Resource> candidateNodes) {
    org.aksw.jena_sparql_api.core.QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.get());
//    qef = new QueryExecutionFactoryDelay(qef, 100);
//    qef = new QueryExecutionFactoryPaginated(qef, 20000);
    final QueryExecution queryExecution = qef.createQueryExecution(query);
    ResultSet resultSet = queryExecution.execSelect();
    AtomicInteger i = new AtomicInteger(0);
    resultSet.forEachRemaining(result -> {
      i.incrementAndGet();
      for (Resource subject : candidateNodes.get(result.getResource("?source"))) {
        subject.addProperty(model.createProperty(result.getResource("?import").getURI()), result.get("?dereffed"));
      }
    });
    queryExecution.close();
    return i.get();
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
    CompletableFuture<Model> future = new ThreadlocalInheritingCompletableFuture<>();
    cache.putIfAbsent(o, future);
    if (cache.get(o) != future) {
      logger.debug("cached future for " + o.getURI());
      try {
        return cache.get(o).get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    logger.debug("no cache for " + o.getURI());
    Model result = ModelFactory.createDefaultModel();
    URL url;
    URLConnection conn;
    try {
      url = new URL(o.getURI());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      future.complete(result);
      return result;
    }
    try {
      conn = url.openConnection();
      conn.setRequestProperty("Accept", "application/rdf+xml");
      conn.setRequestProperty("Accept-Language", "en");
      result.read(conn.getInputStream(), null);
    } catch (ConnectException e) {
      if (e.getMessage().contains("refused")) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    future.complete(result);
    return result;
  }

  private MultiMap<Resource, Resource> getCandidateNodeMap(String lookupPrefix, Property lookUpProperty) {
    MultiMap<Resource, Resource> result = new MultiHashMap<>();
    model.listStatements()
      .filterKeep(statement -> statement.getObject().isURIResource() &&
        statement.getObject().asResource().getURI().startsWith(lookupPrefix) &&
        (lookUpProperty == null || statement.getPredicate().equals(lookUpProperty)))
      .forEachRemaining(stmt -> result.put(stmt.getResource(), stmt.getSubject()));
    return result;
  }

  @Override
  public double predictApplicability(List<Model> inputs, Model target) {
    return learnParameterMap(inputs, target, null).listPropertyObjects(OPERATION).count() > 0 ? 1 : 0;
  }

  @Override
  public List<Model> reverseApply(List<Model> inputs, Model target) {
    Model result = ModelFactory.createDefaultModel();
    Set<Resource> predicatesForDeletion = learnParameterMap(inputs, target, null).listPropertyObjects(OPERATION)
      .map(n -> n.asResource().getPropertyResourceValue(DEREFERENCING_PROPERTY))
      .collect(Collectors.toSet());
    target.listStatements()
      .filterDrop(stmt -> stmt.getSubject().getURI().startsWith(DEFAULT_LOOKUP_PREFIX)
        && predicatesForDeletion.contains(stmt.getPredicate()))
      .forEachRemaining(result::add);
    return List.of(result);
  }

  @Override
  public ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target, ValidatableParameterMap prototype) {
    Model in = inputs.get(0);
    ValidatableParameterMap result = createParameterMap();
    target.listStatements()
      .filterDrop(stmt -> stmt.getObject().isLiteral())
      .mapWith(Statement::getResource)
      .filterKeep(r -> r.getURI().startsWith(DEFAULT_LOOKUP_PREFIX))
      .forEachRemaining(r -> target.listStatements(r, null, (RDFNode) null)
        .filterDrop(stmt -> in.contains(r, stmt.getPredicate(), (RDFNode) null))
        .mapWith(Statement::getPredicate).toSet()
        .forEach(p -> result.add(OPERATION,
          result.createResource()
            .addProperty(LOOKUP_PREFIX, DEFAULT_LOOKUP_PREFIX)
            .addProperty(DEREFERENCING_PROPERTY, p)
        )));
    return result.init();
  }

  @Override
  public DegreeBounds getLearnableDegreeBounds() {
    return getDegreeBounds();
  }

  static void setDefaultLookupPrefix(String defaultLookupPrefix) {
    DEFAULT_LOOKUP_PREFIX = defaultLookupPrefix;
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
      return (Objects.equals(lookupPrefix, other.lookupPrefix))
        && (Objects.equals(lookupProperty, other.lookupProperty));
    }

    @Override
    public int hashCode() {
      return (lookupPrefix == null ? -1 : lookupPrefix.hashCode()) + 13 * (lookupProperty == null ? -1 : lookupProperty.hashCode());
    }

    public String toString() {
      return "(lookupPrefix: " + lookupPrefix + (lookupProperty == null ? "" : ", lookupProperty: " + lookupProperty) + ")";
    }

  }

}
