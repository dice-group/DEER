package org.aksw.deer.enrichments;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import org.aksw.deer.ParametrizedDeerPlugin;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.List;
import java.util.Objects;

/**
 * 
 */
@Extension
public class AuthorityConformationEnrichmentOperator extends AbstractParametrizedEnrichmentOperator implements ParametrizedDeerPlugin {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  private static final Parameter SOURCE_SUBJECT_AUTHORITY = new ParameterImpl("sourceSubjectAuthority");

  private static final Parameter TARGET_SUBJECT_AUTHORITY = new ParameterImpl("targetSubjectAuthority");

  private String sourceSubjectAuthority;

  private String targetSubjectAuthority;


  public AuthorityConformationEnrichmentOperator() {
    super();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
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
   */
  @NotNull
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

  @NotNull
  @Override
  public ParameterMap createParameterMap() {
    return new ParameterMapImpl(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    this.sourceSubjectAuthority = params.getValue(SOURCE_SUBJECT_AUTHORITY);
    this.targetSubjectAuthority = params.getValue(TARGET_SUBJECT_AUTHORITY);
  }

}
