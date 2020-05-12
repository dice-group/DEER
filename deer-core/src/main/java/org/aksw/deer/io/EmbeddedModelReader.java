package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.*;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

/**
 *
 *
 *
 */
@Extension
public class EmbeddedModelReader extends AbstractModelReader {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedModelReader.class);

  public static final Property EMBEDDED_MODEL = DEER.property("embeddedModel");
  public static final Property SERIALIZATION_FORMAT = DEER.property("serializationFormat");
  public static final Property IMPORT_PREFIXES = DEER.property("importPrefixes");

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(EMBEDDED_MODEL)
      .declareProperty(SERIALIZATION_FORMAT)
//      .declareValidationShape(getValidationModelFor(EmbeddedModelReader.class))
      .build();
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    final String embeddedModel = getParameterMap().get(EMBEDDED_MODEL).asLiteral().getString();
    final String serializationFormat  = getParameterMap().getOptional(SERIALIZATION_FORMAT).map(RDFNode::asLiteral).map(Literal::getString).orElse("TTL");
    final boolean importPrefixes  = getParameterMap().getOptional(SERIALIZATION_FORMAT).map(RDFNode::asLiteral).map(Literal::getBoolean).orElse(true);

    Model result = ModelFactory.createDefaultModel();
    //todo: allow importing prefixes from configuration file
    //result.setNsPrefixes(...);
    result.read(new StringReader(embeddedModel), null, serializationFormat);
    return List.of(result);
  }

}
