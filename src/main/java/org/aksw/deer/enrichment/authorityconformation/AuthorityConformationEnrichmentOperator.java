package org.aksw.deer.enrichment.authorityconformation;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.util.Parameter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author sherif
 */
@Extension
public class AuthorityConformationEnrichmentOperator extends AEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(AuthorityConformationEnrichmentOperator.class);

  private String sourceSubjectAuthority;

  private final Parameter SOURCE_SUBJECT_AUTHORITY = new Parameter(
    "sourceSubjectAuthority",
    "Source subject authority to be replaced by Target subject authority",
    "", true, false, x -> this.sourceSubjectAuthority = x);

  private String targetSubjectAuthority;

  private final Parameter TARGET_SUBJECT_AUTHORITY = new Parameter(
    "targetSubjectAuthority",
    "Target subject authority to replace the source subject authority",
    "", true, false, x -> this.targetSubjectAuthority = x);

  public AuthorityConformationEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> process() {
    Model input = models.get(0);
    Model output = ModelFactory.createDefaultModel();
    input.listStatements().forEachRemaining(stmt -> {
      Resource s = stmt.getSubject();
      if (!Objects.equals(sourceSubjectAuthority, "") && s.getURI().startsWith(sourceSubjectAuthority)) {
        s = ResourceFactory.createResource(s.getURI().replaceFirst(sourceSubjectAuthority, targetSubjectAuthority));
      }
      output.add(s, stmt.getPredicate(), stmt.getObject());
    });
    return Lists.newArrayList(output);
  }

  /**
   * @return Most redundant source URI in the input model
   * @author sherif
   */
  private String getMostRedundantUri(Model m) {
    Multiset<Resource> subjectsMultiset = HashMultiset.create();
    ResIterator listSubjects = m.listSubjects();
    while (listSubjects.hasNext()) {
      String authority = listSubjects.next().toString();
      if (authority.contains("#")) {
        //@todo: @sherif, isn't indexOf better here?
        authority = authority.substring(0, authority.lastIndexOf("#"));
      } else {
        authority = authority.substring(0, authority.lastIndexOf("/"));
      }
      subjectsMultiset.add(ResourceFactory.createResource(authority));
    }
    String result = "";
    int max = 0;
    for (Resource r : subjectsMultiset) {
      int i = subjectsMultiset.count(r);
      if (i > max) {
        max = i;
        result = r.getURI();
      }
    }
    return result;
  }

  @Override
  public List<Parameter> getParameters() {
    return Lists.newArrayList(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public String getDescription() {
    return "The purpose of the authority conformation enrichment is to change a specified " +
      "source URI to a specified target URI, for example using " +
      "source URI of 'http://dbpedia.org' and target URI of 'http://example.org' " +
      "changes a resource like 'http://dbpedia.org/Berlin' to 'http://example.org/Berlin'";
  }

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    Map<String, String> parameters = new HashMap<>();
    String s = getMostRedundantUri(source);
    String t = getMostRedundantUri(target);
    if (!Objects.equals(s, t)) {
      parameters.put(SOURCE_SUBJECT_AUTHORITY.getName(), s);
      parameters.put(TARGET_SUBJECT_AUTHORITY.getName(), t);
    }
    return parameters;
  }

}