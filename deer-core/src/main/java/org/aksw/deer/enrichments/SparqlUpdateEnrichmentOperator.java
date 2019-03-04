package org.aksw.deer.enrichments;

import com.google.common.collect.Lists;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.update.UpdateAction;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 *
 *
 */
@Extension
public class SparqlUpdateEnrichmentOperator extends AbstractParameterizedEnrichmentOperator {

  private static final Logger logger = LoggerFactory.getLogger(AuthorityConformationEnrichmentOperator.class);

  public static final Property UPDATE = DEER.property("updateQuery");

  @NotNull
  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(UPDATE)
      .declareValidationShape(getValidationModelFor(SparqlUpdateEnrichmentOperator.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> models) {
    Model model = models.get(0);
    final String updateStatement = getParameterMap().get(UPDATE).asLiteral().getString();
    UpdateAction.parseExecute(updateStatement, model);
    return Lists.newArrayList(model);
  }

//  @NotNull
//  @Override
//  public ParameterMap selfConfig(Model source, Model target) {
//    return createParameterMap();
//  }

}