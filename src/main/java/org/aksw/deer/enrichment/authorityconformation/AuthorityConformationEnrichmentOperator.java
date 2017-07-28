package org.aksw.deer.enrichment.authorityconformation;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author sherif
 */
@Extension
public class AuthorityConformationEnrichmentOperator extends AEnrichmentOperator {

  // parameters keys
  public static final String SOURCE_SUBJECT_AUTHORITY = "sourceSubjectAuthority";
  public static final String SOURCE_SUBJECT_AUTHORITY_DESC = "Source subject authority to be replaced by Target subject authority.";
  public static final String TARGET_SUBJECT_AUTHORITY = "targetSubjectAuthority";
  public static final String TARGET_SUBJECT_AUTHORITY_DESC = "Target subject authority to replace the source subject authority.";
  private static final Logger logger = Logger.getLogger(AuthorityConformationEnrichmentOperator.class);
  // parameters list
  private String sourceSubjectAuthority = "";
  private String targetSubjectAuthority = "";

  public AuthorityConformationEnrichmentOperator() {
    super();
  }

  /**
   * Self configuration
   * Find source/target URI as the most redundant URIs
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  public Map<String, String> selfConfig(Model source, Model target) {
    Map<String, String> parameters = new HashMap<String, String>();
    String s = getMostRedundantUri(source);
    String t = getMostRedundantUri(target);
    if (s != t) {
      sourceSubjectAuthority = s;
      targetSubjectAuthority = t;
      parameters.put(SOURCE_SUBJECT_AUTHORITY, sourceSubjectAuthority);
      parameters.put(TARGET_SUBJECT_AUTHORITY, targetSubjectAuthority);
    }
    return parameters;
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
        authority = authority.substring(0, authority.lastIndexOf("#"));
      } else {
        authority = authority.substring(0, authority.lastIndexOf("/"));
      }
      subjectsMultiset.add(ResourceFactory.createResource(authority));
    }
    Resource result = ResourceFactory.createResource();
    int max = 0;
    for (Resource r : subjectsMultiset) {
      Integer value = subjectsMultiset.count(r);
      if (value > max) {
        max = value;
        result = r;
      }
    }
    return result.toString();
  }

  @Override
  protected List<Model> process() {
    Model model = models.get(0);
    logger.info("--------------- Authority Conformation Module ---------------");

    //Read parameters
    boolean parameterFound = false;
    if (parameters.containsKey(SOURCE_SUBJECT_AUTHORITY) && parameters
      .containsKey(TARGET_SUBJECT_AUTHORITY)) {
      String s = parameters.get(SOURCE_SUBJECT_AUTHORITY);
      String t = parameters.get(TARGET_SUBJECT_AUTHORITY);
      if (!s.equals(t)) {
        sourceSubjectAuthority = s;
        targetSubjectAuthority = t;
        parameterFound = true;
      }
    }
    if (!parameterFound) {
      return Lists.newArrayList(model);
    }

    //Conform Model
    Model conformModel = ModelFactory.createDefaultModel();
    StmtIterator statmentsIter = model.listStatements();
    while (statmentsIter.hasNext()) {
      Statement statment = statmentsIter.nextStatement();
      Resource s = statment.getSubject();
      Property p = statment.getPredicate();
      RDFNode o = statment.getObject();
      // conform subject authority
      if (sourceSubjectAuthority != "" && s.toString().startsWith(sourceSubjectAuthority)) {
        s = ResourceFactory.createResource(
          s.toString().replaceFirst(sourceSubjectAuthority, targetSubjectAuthority));
      }
      conformModel.add(s, p, o);
    }
    model = conformModel;
    return Collections.singletonList(model);
  }

  @Override
  public List<String> getParameters() {
    return Lists.newArrayList(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public List<String> getNecessaryParameters() {
    return Lists.newArrayList(SOURCE_SUBJECT_AUTHORITY, TARGET_SUBJECT_AUTHORITY);
  }

  @Override
  public String getDescription() {
    return null;
  }

}
