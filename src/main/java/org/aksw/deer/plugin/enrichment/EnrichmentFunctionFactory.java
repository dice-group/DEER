package org.aksw.deer.plugin.enrichment;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.plugin.enrichment.authorityconformation.AuthorityConformationEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.dereferencing.DereferencingEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.filter.FilterEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.linking.LinkingEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.nlp.NLPEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.predicateconformation.PredicateConformationEnrichmentFunction;
import org.apache.log4j.Logger;


/**
 * @author sherif
 */
@Deprecated
public class EnrichmentFunctionFactory {

  public static final String DEREFERENCING_MODULE = "org.aksw.deer.resources.dereferencing";
  public static final String LINKING_MODULE = "org.aksw.deer.resources.linking";
  public static final String NLP_MODULE = "nlp";
  public static final String FILTER_MODULE = "filter";
  public static final String AUTHORITY_CONFORMATION_MODULE = "authorityconformation";
  public static final String PREDICATE_CONFORMATION_MODULE = "predicateconformation";
  public static final String DEREFERENCING_MODULE_DESCRIPTION =
    "The purpose of the org.aksw.deer.resources.dereferencing enrichment is to extend the model's Geo-spatial" +
      "information by set of information through specified predicates";
  public static final String LINKING_MODULE_DESCRIPTION =
    "The purpose of the org.aksw.deer.resources.linking enrichment is to enrich a model with additional " +
      "geographic information URIs represented in owl:sameAs predicates";
  public static final String NLP_MODULE_DESCRIPTION =
    "The purpose of the NLP enrichment is to enrich a model with additional Geo-" +
      "spatial information URIs represented by the addedGeoProperty predicates, " +
      "witch by default is geoknow:relatedTo predicates";
  public static final String AUTHORITY_CONFORMATION_MODULE_DESCRIPTION =
    "The purpose of the authority conformation enrichment is to hange a specified source URI " +
      "to a specified target URI, for example using " +
      "source URI of 'http://dbpedia.org' and target URI of 'http://geolift.org' " +
      "changes a resource like 'http://dbpedia.org/Berlin' to 'http://geolift.org/Berlin'";
  public static final String PREDICATE_CONFORMATION_MODULE_DESCRIPTION =
    "The purpose of the predicate conformation enrichment is to change a set of source predicates to a set of target predicates."
      +
      "For example, all rdfs:label can be conformed to become skos:prefLabel";
  public static final String FILTER_MODULE_DESCRIPTION =
    "Runs a set of triples patterns against an input model to filter some triples out " +
      "of it and export them to an output model. For example running triple pattern " +
      "'?s <http://dbpedia.org/ontology/abstract> ?o' againt an input model containing " +
      "'http://dbpedia.org/resource/Berlin' will generate output model containing only " +
      "Berlin's abstracts of DBpedia";
  private static final Logger logger = Logger.getLogger(EnrichmentFunctionFactory.class.getName());

  /**
   * @return a specific enrichment instance given its enrichment's name
   * @author sherif
   */
  public static IEnrichmentFunction createModule(String name) {
    logger.info("Creating Module with name " + name);

    if (name.equalsIgnoreCase(DEREFERENCING_MODULE)) {
      return new DereferencingEnrichmentFunction();
    }
    if (name.equalsIgnoreCase(LINKING_MODULE)) {
      return new LinkingEnrichmentFunction();
    }
    if (name.equalsIgnoreCase(NLP_MODULE)) {
      return new NLPEnrichmentFunction();
    }
    if (name.equalsIgnoreCase(AUTHORITY_CONFORMATION_MODULE)) {
      return new AuthorityConformationEnrichmentFunction();
    }
    if (name.equalsIgnoreCase(PREDICATE_CONFORMATION_MODULE)) {
      return new PredicateConformationEnrichmentFunction();
    }
    if (name.equalsIgnoreCase(FILTER_MODULE)) {
      return new FilterEnrichmentFunction();
    }
    //TODO Add any new enrichment here

    logger.error("Sorry, The enrichment " + name + " is not yet implemented. Exit with error ...");
    System.exit(1);
    return null;
  }

  public static String getDescription(String name) {
    String description = "";

    if (name.equalsIgnoreCase(DEREFERENCING_MODULE)) {
      description = DEREFERENCING_MODULE_DESCRIPTION;
    } else if (name.equalsIgnoreCase(LINKING_MODULE)) {
      description = LINKING_MODULE_DESCRIPTION;
    } else if (name.equalsIgnoreCase(NLP_MODULE)) {
      description = NLP_MODULE_DESCRIPTION;
    } else if (name.equalsIgnoreCase(AUTHORITY_CONFORMATION_MODULE)) {
      description = AUTHORITY_CONFORMATION_MODULE_DESCRIPTION;
    } else if (name.equalsIgnoreCase(PREDICATE_CONFORMATION_MODULE)) {
      description = PREDICATE_CONFORMATION_MODULE_DESCRIPTION;
    } else if (name.equalsIgnoreCase(FILTER_MODULE)) {
      description = FILTER_MODULE_DESCRIPTION;
    }

    return description;
  }

  /**
   * @return list of names of all implemented enrichment
   * @author sherif
   */
  public static List<String> getNames() {
    List<String> result = new ArrayList<>();
    result.add(DEREFERENCING_MODULE);
    result.add(LINKING_MODULE);
    result.add(NLP_MODULE);
    result.add(AUTHORITY_CONFORMATION_MODULE);
    result.add(PREDICATE_CONFORMATION_MODULE);
    result.add(FILTER_MODULE);
    //TODO Add any new enrichment here
    return result;
  }

  /**
   * @return list of instances of all implemented enrichment
   * @author sherif
   */
  public static List<IEnrichmentFunction> getImplementations() {
    List<IEnrichmentFunction> result = new ArrayList<>();
    result.add(new DereferencingEnrichmentFunction());
    result.add(new LinkingEnrichmentFunction());
    result.add(new NLPEnrichmentFunction());
    result.add(new AuthorityConformationEnrichmentFunction());
    result.add(new PredicateConformationEnrichmentFunction());
    result.add(new FilterEnrichmentFunction());
    //TODO Add any new enrichment here
    return result;
  }
}