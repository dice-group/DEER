package org.aksw.deer.enrichments;

import com.github.therapi.runtimejavadoc.RetainJavadoc;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 *     <b>{@code :lookUpPrefix} [required]</b>
 *     <i>range: string</i>
 *     <br>
 *     Determines the starting resources {@code ?x} as all objects of triples having
 *     the value of {@code :lookUpProperty} as predicate.
 * </blockquote>

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
 * <blockquote>
 *     <b>{@code :endpoint} </b>
 *     <i>range: string</i>
 *     <br>
 *     URL of the SPARQL endpoint to use for this dereferencing operation.
 *     If not given, this operator tries to infer the endpoint from the starting resources URIs.
 * </blockquote>
 *
 * <h2>Example</h2>
 *
 */
@Extension @RetainJavadoc
public class DereferencingEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(DereferencingEnrichmentOperator.class);

  // @todo: implement this someday.. requires util endpoint discovery
  //  * <blockquote>
  //  *     <b>{@code :lookUpProperty} [required]</b>
  //  *     <i>range: resource</i>
  //  *     <br>
  //  *     Determines the starting resources {@code ?x} as all objects of triples having
  //  *     the value of {@code :lookUpProperty} as predicate.
  //  * </blockquote>
  //  private static final Property LOOKUP_PROPERTY = DEER.property("lookUpProperty");

  private static final Property LOOKUP_PREFIX = DEER.property("lookUpPrefix");

  private static final Property DEREFERENCING_PROPERTY = DEER.property("dereferencingProperty");

  private static final Property IMPORT_PROPERTY = DEER.property("importProperty");

  private static final Parameter OPERATIONS = new ParameterImpl("operations",
    new DictListParameterConversion(LOOKUP_PREFIX, DEREFERENCING_PROPERTY,
      IMPORT_PROPERTY), true);

  private static final String DEFAULT_LOOKUP_PREFIX = "http://dbpedia.org/resource";

  private static final Set<Property> ignoredProperties = new HashSet<>(Arrays.asList(OWL.sameAs));

  private List<Map<Property, RDFNode>> operations;

  private Model model;

  public DereferencingEnrichmentOperator() {
    super();
  }

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
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
    this.operations = params.getValue(OPERATIONS);

  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    model = ModelFactory.createDefaultModel().add(models.get(0));
    operations.forEach(this::runOperation);
    return Lists.newArrayList(model);
  }

  private void runOperation(Map<Property, RDFNode> op) {
    // get configuration for this operation
    String lookUpPrefix = op.get(LOOKUP_PREFIX) == null
      ? DEFAULT_LOOKUP_PREFIX : op.get(LOOKUP_PREFIX).toString();
    Property dereferencingProperty = op.get(DEREFERENCING_PROPERTY) == null
      ? null : op.get(DEREFERENCING_PROPERTY).as(Property.class);
    Property importProperty = op.get(IMPORT_PROPERTY) == null
      ? dereferencingProperty : op.get(IMPORT_PROPERTY).as(Property.class);
    // execute this operation
    if (dereferencingProperty != null) {
      for (Resource s : getCandidateNodesByPrefix(lookUpPrefix)) {
        for (RDFNode o : getEnrichmentValuesFor(s, dereferencingProperty)) {
          s.addProperty(importProperty, o);
        }
      }
    }
  }

  private List<Resource> getCandidateNodesByPrefix (String lookupPrefix) {
    // old way with util:
    //      "SELECT * " +
    //      "WHERE { ?s ?p ?o . FILTER (isURI(?o)) . " +
    //      "FILTER (STRSTARTS(STR(?o), \"" + lookupPrefix + "\"))}";
    return model.listStatements()
      .mapWith(Statement::getObject)
      .filterKeep(RDFNode::isURIResource)
      .mapWith(RDFNode::asResource)
      .filterKeep(o -> o.getURI().startsWith(lookupPrefix))
      .toList();
  }


  private List<RDFNode> getEnrichmentValuesFor(Resource resource, Property dereferencingProperty) {
    try {
      URLConnection conn = new URL(resource.getURI()).openConnection();
      conn.setRequestProperty("Accept", "application/rdf+xml");
      conn.setRequestProperty("Accept-Language", "en");
      return ModelFactory.createDefaultModel()
        .read(conn.getInputStream(), null)
        .listStatements(resource, dereferencingProperty, (RDFNode) null)
        .mapWith(Statement::getObject)
        .filterDrop(v -> v.isLiteral() && !Arrays.asList("en","")
          .contains(v.asLiteral().getLanguage().toLowerCase()))
        .toList();
    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private Set<Property> getPropertyDifference(Model source, Model target) {
    Function<Model, Set<Property>> getProperties =
      (m) -> m.listStatements().mapWith(Statement::getPredicate).toSet();
    return Sets.difference(getProperties.apply(source), getProperties.apply(target))
      .stream().filter(p -> !ignoredProperties.contains(p)).collect(Collectors.toSet());
  }

}
