package org.aksw.deer.enrichment;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import org.aksw.deer.parameter.Parameter;
import org.aksw.deer.parameter.ParameterImpl;
import org.aksw.deer.parameter.DefaultParameterMap;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

import java.util.List;
import java.util.Objects;

/**
 * 
 */
@Extension
public class AuthorityConformationEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(AuthorityConformationEnrichmentOperator.class);

  private static final Parameter SOURCE_SUBJECT_AUTHORITY = new ParameterImpl("sourceSubjectAuthority");

  private static final Parameter TARGET_SUBJECT_AUTHORITY = new ParameterImpl("targetSubjectAuthority");

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

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap parameters = createParameterMap();
    String s = getMostRedundantUri(source);
    String t = getMostRedundantUri(target);
    if (!Objects.equals(s, t)) {
      parameters.setValue(SOURCE_SUBJECT_AUTHORITY, s);
      parameters.setValue(TARGET_SUBJECT_AUTHORITY, s);
    }
    return parameters;
  }

  @Override
  public ParameterMap createParameterMap() {
    return new DefaultParameterMap(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public void accept(ParameterMap params) {
    this.sourceSubjectAuthority = params.getValue(SOURCE_SUBJECT_AUTHORITY);
    this.targetSubjectAuthority = params.getValue(TARGET_SUBJECT_AUTHORITY);
  }

}