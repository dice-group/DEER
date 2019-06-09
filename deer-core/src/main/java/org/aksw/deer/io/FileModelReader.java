package org.aksw.deer.io;

import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Optional;

/**
 *
 *
 *
 */
@Extension
public class FileModelReader extends AbstractModelReader {

  private static final Logger logger = LoggerFactory.getLogger(FileModelReader.class);

  public static final Property FROM_PATH = DEER.property("fromPath");
  public static final Property FROM_URI = DEER.property("fromUri");

  @Override
  public @NotNull
  ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(FROM_PATH)
      .declareProperty(FROM_URI)
      .declareValidationShape(getValidationModelFor(FileModelReader.class))
      .build();
  }

  @NotNull
  @Override
  protected List<Model> safeApply(List<Model> data) {
    final Optional<String> path = getParameterMap().getOptional(FROM_PATH)
      .map(RDFNode::asLiteral).map(Literal::getString);
    final Optional<String> uri = getParameterMap().getOptional(FROM_URI)
      .map(RDFNode::asLiteral).map(Literal::getString);
    String locator = "";
    if (path.isPresent()) {
      try {
        locator = injectWorkingDirectory(path.get());
      } catch (InvalidPathException e) {
        logger.info("Invalid path {} specified in {} {}!", path, this.getType(), this.getId());
        logger.info("Gracefully trying to use it as URI to load the model.");
        locator = path.get();
      }
    } else if (uri.isPresent()) {
      try {
        locator = new URI(uri.get()).toString();
      } catch (URISyntaxException e) {
        logger.info("Invalid URI {} specified in {} {}!", path, this.getType(), this.getId());
        logger.info("Gracefully trying to use it as path to load the model.");
        locator = uri.get();
      }
    }
    final long startTime = System.currentTimeMillis();
    Model result = ModelFactory.createDefaultModel().read(locator);
    logger.info("Loading {} is done in {}ms.", locator,
      (System.currentTimeMillis() - startTime));
    return List.of(result);
  }
}
