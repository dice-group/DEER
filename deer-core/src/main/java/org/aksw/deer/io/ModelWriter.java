package org.aksw.deer.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ModelWriter extends WorkingDirectoryInjectedIO implements Consumer<Model> {

  private static final Logger logger = LoggerFactory.getLogger(ModelWriter.class);

  private String format = "TTL";
  private String outputFile;

  public ModelWriter (String outputFile, String format) {
    this(outputFile);
    this.format = format;
  }

  public ModelWriter (String outputFile) {
    this.outputFile = injectWorkingDirectory(outputFile);
  }

  @Override
  public void accept(Model model) {
    try {
      logger.info("Saving dataset to " + outputFile + "...");
      final long starTime = System.currentTimeMillis();
      File writingDir = new File(outputFile).getParentFile();
      if (writingDir != null && !writingDir.exists()) {
        writingDir.mkdirs();
      }
      model.write(new FileWriter(outputFile), format);
      logger.info("Saving dataset done in " + (System.currentTimeMillis() - starTime) + "ms.");
    } catch (IOException e) {
      throw new RuntimeException("Encountered problem while trying to write dataset to " +
        outputFile, e);
    }
  }
}
