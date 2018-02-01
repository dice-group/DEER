package org.aksw.deer.enrichment;

import com.google.common.collect.Lists;
import org.aksw.deer.parameter.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

import java.util.List;

/**
 * Basic implementation of a fusion operator for geospatial properties.
 *
 */
@Extension
public class GeoFusionEnrichmentOperator extends AbstractEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(GeoFusionEnrichmentOperator.class);

  /**
   * required parameter, value is one of {@link GeoFusionAction}
   */
  private static final Parameter FUSION_ACTION = new ParameterImpl("fusionAction");
  /**
   * optional parameter, boolean, default <code>true</code>
   */
  private static final Parameter MERGE_OTHER_STATEMENTS = new ParameterImpl("mergeOtherStatements",
    StringParameterConversion.getInstance(), false);

  private GeoFusionAction fusionAction;
  private boolean mergeOtherStatements;

  /**
   * Geospatial fusion actions:
   *
   * <ul>
   * <li>{@link #takeA} always use geometry from first model</li>
   * <li>{@link #takeB} always use geometry from second model, if ambiguous
   * take most detailed</li>
   * <li>{@link #takeAll} merge all geometries</li>
   * <li>{@link #takeMostDetailed} use most detailed geometry from any model, e.g., in
   * terms of lexical length of latitude and longitude values</li>
   * </ul>
   *
   */
  public enum GeoFusionAction {
    takeA, takeB, takeAll, takeMostDetailed
  }

  /**
   * Helper class storing a candidate geometry
   */
  private static class CandidateGeometry {

    private final Resource subject;
    private final Model geometryModel;

    CandidateGeometry(Resource subject, Model geometryModel) {
      super();
      this.subject = subject;
      this.geometryModel = geometryModel;
    }

    Resource getSubject() {
      return subject;
    }

    Model getGeometryModel() {
      return geometryModel;
    }

    @Override
    public String toString() {
      return "CandidateGeometry [subject=" + subject + ", geometryModel=" + geometryModel + "]";
    }

  }

  @Override
  protected List<Model> process() {
    logger.info("Invoking GeoFusionOperator of {} models with fusionAction {} and mergeOtherStatements {}", models.size(), fusionAction, mergeOtherStatements);

    try {
//      logger.info(" Model A: {}", models.get(0));
//      logger.info(" Model B: {}", models.get(1));
    } catch (Exception e) {
      logger.error("GeoFusionOperator expected two models, failed:", e);
      return Lists.newArrayList();
    }

    GeoFusionAction action = fusionAction;

    // TODO get rid of this quick fix: if there's no sameAs in model A, swap them ;-)
    // won't be necessary with new DEER configs supporting lists of datasets/parameters
    if (!models.get(0).contains(null, OWL.sameAs)) {
      logger.info("No owl:sameAs statements detected in first model, attempting to swap model A and model B");
      Model temp = models.get(1);
      models.set(1, models.get(0));
      models.set(0, temp);
    }

    // Fuse geometries
    Model targetModel = ModelFactory.createDefaultModel();
    List<Resource> visitedResources = Lists.newArrayList();
    ResIterator subjects = models.get(0).listSubjects();
    while (subjects.hasNext()) {
      Resource subject = subjects.next();
      List<Resource> sameAsResources = getSameAsResources(models.get(0), subject);
      List<CandidateGeometry> candidateGeometries = getCandidateGeometries(models.get(0), models.get(1), subject,
        sameAsResources);

      fuseCandidateGeometries(models.get(0), models.get(1), subject, sameAsResources, candidateGeometries, action,
        mergeOtherStatements, targetModel);

      visitedResources.add(subject);
      visitedResources.addAll(sameAsResources);
    }

    addNonVisitedResources(models.get(1), targetModel, visitedResources, mergeOtherStatements);

//    logger.info("Target model returned by GeoFusionOperator: {}", targetModel);

    return Lists.newArrayList(targetModel);
  }

  @Override
  public ParameterMap selfConfig(Model source, Model target) {
    ParameterMap result = createParameterMap();
    result.setValue(FUSION_ACTION, GeoFusionAction.takeMostDetailed.name());
    result.setValue(MERGE_OTHER_STATEMENTS, "true");
    return result;
  }

  @Override
  public void accept(ParameterMap params) {
    this.fusionAction = GeoFusionAction.valueOf(params.getValue(FUSION_ACTION));
    this.mergeOtherStatements = Boolean.valueOf(params.getValue(MERGE_OTHER_STATEMENTS, "true"));
  }

  @Override
  public ParameterMap createParameterMap() {
    return new DefaultParameterMap(FUSION_ACTION, MERGE_OTHER_STATEMENTS);
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(2, 2, 1, 1);
  }

  private void fuseCandidateGeometries(Model model, Model model2, Resource subject, List<Resource> sameAsResources,
                                       List<CandidateGeometry> candidateGeometries, GeoFusionAction action, boolean mergeOtherStatements,
                                       Model targetModel) {
    CandidateGeometry selectedCandidateGeometry = selectCandidateGeometry(model, candidateGeometries, subject,
      action);
    if (selectedCandidateGeometry != null) {
      logger.info("Selected candidate geometry: {}", selectedCandidateGeometry);
    }
    // if not all geometries are taken, compute dropped geometries
    Model droppedGeometries = action.equals(GeoFusionAction.takeAll) ? ModelFactory.createDefaultModel()
      : getDroppedGeometries(candidateGeometries, selectedCandidateGeometry);

    if (!mergeOtherStatements) {
      // only use selected geometry statements, possibly adapted to
      // subject
      StmtIterator geometryStatements = selectedCandidateGeometry.getGeometryModel().listStatements();
      while (geometryStatements.hasNext()) {
        Statement statement = geometryStatements.next();
        targetModel.add(subject, statement.getPredicate(), statement.getObject());
      }
      return;
    }

    // add all statements of subject from model if they haven't been dropped
    StmtIterator subjectStatements = model.listStatements(subject, null, (RDFNode) null);
    while (subjectStatements.hasNext()) {
      Statement statement = subjectStatements.next();
      if (!droppedGeometries.contains(statement)) {
        targetModel.add(statement);
      }
    }
    // iterate over all sameAs resources from model2 and fuse them if they
    // haven't been dropped
    for (Resource sameAsResource : sameAsResources) {
      StmtIterator sameAsStatements = model2.listStatements(sameAsResource, null, (RDFNode) null);
      while (sameAsStatements.hasNext()) {
        Statement statement = sameAsStatements.next();
        if (!droppedGeometries.contains(statement)) {
          targetModel.add(subject, statement.getPredicate(), statement.getObject());
        }
      }
    }
  }

  private void addNonVisitedResources(Model model2, Model targetModel, List<Resource> visitedResources,
                                      boolean mergeOtherStatements) {
    // add non-visited resources
    List<Resource> nonVisitedSubjects = model2.listSubjects().toList();
    nonVisitedSubjects.removeAll(visitedResources);
    for (Resource nonVisitedSubject : nonVisitedSubjects) {
      if (mergeOtherStatements) {
        targetModel.add(model2.listStatements(nonVisitedSubject, null, (RDFNode) null));
      } else {
        // extract geometry
        Model geometryModel = getGeometry(model2, nonVisitedSubject);
        targetModel.add(geometryModel);
      }
    }
  }

  private CandidateGeometry selectCandidateGeometry(Model model, List<CandidateGeometry> candidateGeometries,
                                                    Resource subject, GeoFusionAction action) {
    CandidateGeometry selected = null;
    int score = -1; // score for most detailed geometry
    if (action.equals(GeoFusionAction.takeAll)) {
      Model mergedGeometries = ModelFactory.createDefaultModel();
      for (CandidateGeometry candidateGeometry : candidateGeometries) {
        mergedGeometries.add(candidateGeometry.geometryModel);
      }
      return new CandidateGeometry(subject, mergedGeometries);
    }
    for (CandidateGeometry candidateGeometry : candidateGeometries) {
      boolean isFromA = candidateGeometry.subject.equals(subject);
      if (action.equals(GeoFusionAction.takeA) && isFromA) {
        return candidateGeometry;
      }
      if (action.equals(GeoFusionAction.takeB) && isFromA) {
        continue;
      }
      int candidateScore = computeCandidateScore(model, candidateGeometry);
      if (candidateScore > score) {
        score = candidateScore;
        selected = candidateGeometry;
      }
    }
    return selected;
  }

  private Model getDroppedGeometries(List<CandidateGeometry> candidateGeometries,
                                     CandidateGeometry selectedCandidateGeometry) {
    Model droppedGeometriesModel = ModelFactory.createDefaultModel();
    for (CandidateGeometry candidateGeometry : candidateGeometries) {
      if (!candidateGeometry.getSubject().equals(selectedCandidateGeometry.getSubject())) {
        droppedGeometriesModel.add(candidateGeometry.getGeometryModel());
      }
    }
    return droppedGeometriesModel;
  }

  private int computeCandidateScore(Model model, CandidateGeometry candidateGeometry) {
    NodeIterator latitudeNodes = candidateGeometry.getGeometryModel().listObjectsOfProperty(null,
      new PropertyImpl(model.expandPrefix("geo:lat")));
    NodeIterator longitudeNodes = candidateGeometry.getGeometryModel().listObjectsOfProperty(null,
      new PropertyImpl(model.expandPrefix("geo:long")));
    int latScore = -1, longScore = -1;
    // very simple heuristic: if both latitude and longitude are there,
    // score is the sum of both literal lengths
    while (latitudeNodes.hasNext()) {
      RDFNode latitudeNode = latitudeNodes.next();
      try {
        latScore = latitudeNode.asLiteral().getLexicalForm().length();
      } catch (Exception e) {
        logger.warn("Unexpected issue parsing latitude literal node " + latitudeNode, e);
      }
    }
    if (latScore == -1) {
      logger.warn("No latitude node found in {}, returning score -1", candidateGeometry);
      return -1;
    }
    while (longitudeNodes.hasNext()) {
      RDFNode longitudeNode = longitudeNodes.next();
      try {
        longScore = longitudeNode.asLiteral().getLexicalForm().length();
      } catch (Exception e) {
        logger.warn("Unexpected issue parsing longitude literal node " + longitudeNode, e);
      }
    }
    if (longScore == -1) {
      logger.warn("No longitude node found in {}, returning score -1", candidateGeometry);
      return -1;
    }
    return latScore + longScore;
  }

  private Model getGeometry(Model model, Resource subject) {
    List<Statement> latitudes = model
      .listStatements(subject, new PropertyImpl(model.expandPrefix("geo:lat")), (RDFNode) null).toList();
    List<Statement> longitudes = model
      .listStatements(subject, new PropertyImpl(model.expandPrefix("geo:long")), (RDFNode) null).toList();
    return ModelFactory.createDefaultModel().add(latitudes).add(longitudes);
  }

  private List<CandidateGeometry> getCandidateGeometries(Model sourceA, Model sourceB, Resource subject,
                                                         List<Resource> sameAsResources) {
    List<CandidateGeometry> candidateGeometries = Lists.newArrayList();
    Model sourceAGeometry = getGeometry(sourceA, subject);
    if (!sourceAGeometry.isEmpty()) {
      // add geometry from source A
      candidateGeometries.add(new CandidateGeometry(subject, sourceAGeometry));
    }
    // get sameAs subjects of sourceB
    for (Resource sameAs : sameAsResources) {
      // if they have a geometry, add them as candidates
      Model sameAsGeometry = getGeometry(sourceB, sameAs);
      if (!sameAsGeometry.isEmpty()) {
        candidateGeometries.add(new CandidateGeometry(sameAs, sameAsGeometry));
      }
    }
    return candidateGeometries;
  }

  private List<Resource> getSameAsResources(Model sourceA, Resource subject) {
    return sourceA.listObjectsOfProperty(subject, OWL.sameAs)
      .filterKeep(RDFNode::isURIResource).mapWith(RDFNode::asResource).toList();
  }

}