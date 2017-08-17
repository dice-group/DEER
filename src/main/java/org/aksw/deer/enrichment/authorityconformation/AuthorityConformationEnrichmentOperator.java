package org.aksw.deer.enrichment.authorityconformation;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Objects;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.parameter.BaseParameter;
import org.aksw.deer.parameter.BaseParameterMap;
import org.aksw.deer.parameter.JenaBackedParameterMap;
import org.aksw.deer.parameter.JenaResourceConsumingParameter;
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
public class AuthorityConformationEnrichmentOperator extends AbstractEnrichmentOperator<JenaBackedParameterMap> {

  private static final Logger logger = Logger.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final BaseParameter SOURCE_SUBJECT_AUTHORITY = new BaseParameter(
    "sourceSubjectAuthority",
    "Source subject authority to be replaced by Target subject authority",
    true);

  public static final BaseParameter TARGET_SUBJECT_AUTHORITY = new BaseParameter(
    "targetSubjectAuthority",
    "Target subject authority to replace the source subject authority",
    true);

  private String sourceSubjectAuthority;

  private String targetSubjectAuthority;


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
        String conformedUri = s.getURI().replaceFirst(sourceSubjectAuthority, targetSubjectAuthority);
        s = ResourceFactory.createResource(conformedUri);
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
  public JenaBackedParameterMap selfConfig(Model source, Model target) {
    JenaBackedParameterMap parameters = createParameterMap();
    String s = getMostRedundantUri(source);
    String t = getMostRedundantUri(target);
    if (!Objects.equals(s, t)) {
      parameters.setValue(SOURCE_SUBJECT_AUTHORITY, s);
      parameters.setValue(TARGET_SUBJECT_AUTHORITY, s);
    }
    return parameters;
  }

  @Override
  public JenaBackedParameterMap createParameterMap() {
    JenaBackedParameterMap result = new JenaBackedParameterMap();
    result.addParameter(SOURCE_SUBJECT_AUTHORITY);
    result.addParameter(TARGET_SUBJECT_AUTHORITY);
    return result;
  }

  @Override
  public void accept(JenaBackedParameterMap baseParameterMap) {
    this.sourceSubjectAuthority = baseParameterMap.getValue(SOURCE_SUBJECT_AUTHORITY);
    this.targetSubjectAuthority = baseParameterMap.getValue(TARGET_SUBJECT_AUTHORITY);
  }
}