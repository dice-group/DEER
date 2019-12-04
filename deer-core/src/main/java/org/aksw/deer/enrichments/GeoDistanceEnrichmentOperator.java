package org.aksw.deer.enrichments;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.limes.core.measures.mapper.pointsets.OrthodromicDistance;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PropertyNotFoundException;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An {@code EnrichmentOperator} to enrich models with distances
 * <p>
 * The {@code GeoDistanceEnrichmentOperator} ...
 */
@Extension
public class GeoDistanceEnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(GeoDistanceEnrichmentOperator.class);

  public static final Property SELECT_PREDICATE = DEER.property("selectPredicate");

  public static final Property DISTANCE_PREDICATE = DEER.property("distancePredicate");

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(SELECT_PREDICATE)
      .declareProperty(DISTANCE_PREDICATE)
      .declareValidationShape(getValidationModelFor(GeoDistanceEnrichmentOperator.class))
      .build();
  }

//  @Override
//  public //  ParameterMap learnParameterMap(Model source, Model target) {
//    return ParameterMap.EMPTY_INSTANCE;
//  }

  /**
   * @param models
   * @return model enriched with distances
   */
  protected List<Model> safeApply(List<Model> models) {
    final Property selectPredicate = getParameterMap().get(SELECT_PREDICATE).as(Property.class);
    final Property distancePredicate = getParameterMap().get(DISTANCE_PREDICATE).as(Property.class);
    models.get(0).listStatements(null, selectPredicate, (RDFNode) null)
      .filterKeep(stmt -> stmt.getObject().isResource()).toList()
      .forEach(stmt -> enrichWithDistance(stmt, models.get(0), distancePredicate));
    return models;
  }

  private void enrichWithDistance(Statement stmt, Model model, Property distancePredicate) {
    final String ns = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    final Property lat = model.createProperty(ns, "lat");
    final Property lon = model.createProperty(ns, "long");
    try {
      double aLat = stmt.getSubject().getRequiredProperty(lat).getDouble();
      double aLong = stmt.getSubject().getRequiredProperty(lon).getDouble();
      double bLat = stmt.getObject().asResource().getRequiredProperty(lat).getDouble();
      double bLong = stmt.getObject().asResource().getRequiredProperty(lon).getDouble();
      stmt.getSubject().addProperty(distancePredicate, OrthodromicDistance.getDistanceInDegrees(aLat, aLong, bLat, bLong) + "km");
    } catch (PropertyNotFoundException e) {
      logger.info("Could not compute distance between " + stmt.getSubject().getURI() + " and " + stmt.getResource().getURI() + "! (Missing lat, long!");
    }
  }

}
