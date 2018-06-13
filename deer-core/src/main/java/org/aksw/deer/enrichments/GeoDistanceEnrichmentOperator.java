package org.aksw.deer.enrichments;

import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.aksw.limes.core.measures.mapper.pointsets.OrthodromicDistance;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
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
public class GeoDistanceEnrichmentOperator extends AbstractParametrizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(GeoDistanceEnrichmentOperator.class);

  private static final Parameter SELECT_PREDICATE = new ParameterImpl("selectPredicate");

  private static final Parameter DISTANCE_PREDICATE = new ParameterImpl("distancePredicate");

  private Property selectPredicate;

  private Property distancePredicate;

  @Override
  public @NotNull
  ParameterMap createParameterMap() {
    return new ParameterMapImpl(SELECT_PREDICATE, DISTANCE_PREDICATE);
  }

  @Override
  public void validateAndAccept(@NotNull ParameterMap params) {
    this.selectPredicate = ResourceFactory.createProperty(params.getValue(SELECT_PREDICATE));
    this.distancePredicate = ResourceFactory.createProperty(params.getValue(DISTANCE_PREDICATE));
  }

  @Override
  public @NotNull
  ParameterMap selfConfig(Model source, Model target) {
    return ParameterMap.EMPTY_INSTANCE;
  }

  /**
   * @param models
   * @return model enriched with distances
   */
  protected List<Model> safeApply(List<Model> models) {
    models.get(0).listStatements(null, selectPredicate, (RDFNode) null)
      .filterKeep(stmt -> stmt.getObject().isResource()).toList()
      .forEach(stmt -> enrichWithDistance(stmt, models.get(0)));
    return models;
  }

  private void enrichWithDistance(Statement stmt, Model model) {
    final String ns = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    final Property lat = model.createProperty(ns, "lat");
    final Property lon = model.createProperty(ns, "long");
    double aLat = stmt.getSubject().getProperty(lat).getDouble();
    double aLong = stmt.getSubject().getProperty(lon).getDouble();
    double bLat = stmt.getObject().asResource().getProperty(lat).getDouble();
    double bLong = stmt.getObject().asResource().getProperty(lon).getDouble();
    stmt.getSubject().addProperty(distancePredicate, OrthodromicDistance.getDistanceInDegrees(aLat, aLong, bLat, bLong) + "km");
  }

}
