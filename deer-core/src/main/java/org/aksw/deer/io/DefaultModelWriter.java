package org.aksw.deer.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.aksw.faraday_cage.Execution;
import org.aksw.faraday_cage.parameter.Parameter;
import org.aksw.faraday_cage.parameter.ParameterImpl;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParameterMapImpl;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Extension
public class DefaultModelWriter extends WorkingDirectoryInjectedIO {

  private static final Logger logger = LoggerFactory.getLogger(DefaultModelWriter.class);

  private static final Parameter OUTPUT_FILE = new ParameterImpl("outputFile");
  private static final Parameter OUTPUT_FORMAT = new ParameterImpl("outputFormat", false);

  private String outputFormat;
  private String outputFile;

  @Override
  public @NotNull ParameterMap createParameterMap() {
    return new ParameterMapImpl(OUTPUT_FILE, OUTPUT_FORMAT);
  }

  @Override
  protected List<Model> safeApply(List<Model> data) {
    return Execution.toMultiExecution(this::write).apply(data);
  }

  @Override
  protected void validateAndAccept(@NotNull ParameterMap parameterMap) {
    outputFile = parameterMap.getValue(OUTPUT_FILE);
    outputFile = injectWorkingDirectory(outputFile);
    outputFormat = parameterMap.getValue(OUTPUT_FORMAT, "TTL");
  }

  private Model write(Model model) {
    try {
      logger.info("Saving dataset to " + outputFile + "...");
      final long starTime = System.currentTimeMillis();
      File writingDir = new File(outputFile).getParentFile();
      if (writingDir != null && !writingDir.exists()) {
        writingDir.mkdirs();
      }
      model.write(new FileWriter(outputFile), outputFormat);
      logger.info("Saving dataset done in " + (System.currentTimeMillis() - starTime) + "ms.");
    } catch (IOException e) {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        outputFile, e);
    }
    return model;
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,1,0,1);
  }

}
