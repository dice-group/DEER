package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.deer.parameter.*;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

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
 *         where i is the property name under which they should be inserted.</li>
 * </ol>
 * <h2>Configuration</h2>
 *
 * <h3>{@code :operations}</h3>
 *
 * A {@link org.aksw.deer.parameter.DictListParameterConversion DictList}.
 *
 * Each entry in the {@code DictList} corresponds to one dereferencing operation, allowing multiple
 * dereferencing operations being carried out by a single {@code DereferencingEnrichmentOperator}.
 * Each entry may contain the following properties:
 *
 * <blockquote>
 *     <b>{@code :lookUpProperty} [required]</b>
 *     <i>range: resource</i>
 *     <br>
 *     Determines the starting resources {@code ?x} as all objects of triples having
 *     the value of {@code :lookUpProperty} as predicate.
 * </blockquote>

 * <blockquote>
 *     <b>{@code :dereferencingPropertyPath} [required]</b>
 *     <i>range: string</i>
 *     <br>
 *     Look up the values to be imported to the local model in the external knowledge base
 *     using the property path defined by the value of {@code :dereferencingPropertyPath}.
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
@Extension
public class DereferencingEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(DereferencingEnrichmentOperator.class);

  private static final Property LOOKUP_PROPERTY = DEER.property("lookUpProperty");

  private static final Property DEREFERENCING_PROPERTY_PATH = DEER.property("dereferencingPropertyPath");

  private static final Property IMPORT_PROPERTY = DEER.property("importProperty");

  private static final Parameter OPERATIONS = new ParameterImpl("operations",
    new DictListParameterConversion(LOOKUP_PROPERTY, DEREFERENCING_PROPERTY_PATH, IMPORT_PROPERTY),
    true);

  private static Set<Property> ignoredProperties = new HashSet<>(Arrays.asList(OWL.sameAs));

  private Map<Property, RDFNode> operations;

  public DereferencingEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> process() {
    Model input = models.get(0);
    Model output = ModelFactory.createDefaultModel();

    return Lists.newArrayList(output);
  }

  private Set<Property> getPropertyDifference(Model source, Model target) {
    Function<Model, Set<Property>> getProperties =
      (m) -> m.listStatements().mapWith(Statement::getPredicate).toSet();
    return Sets.difference(getProperties.apply(source), getProperties.apply(target))
      .stream().filter(p -> !ignoredProperties.contains(p)).collect(Collectors.toSet());
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
    for (Property property : propertyDifference) {

    }
    return parameters;
  }

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(OPERATIONS);
  }

  @Override
  public void accept(@NotNull ParameterMap params) {
    this.operations = params.getValue(OPERATIONS);

  }

}