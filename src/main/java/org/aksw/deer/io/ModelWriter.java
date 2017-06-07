package org.aksw.deer.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
public class ModelWriter implements Consumer<Model> {

  private static final Logger logger = Logger.getLogger(ModelWriter.class.getName());

  private String subDir = "";
  private String format;
  private String outputFile;

  public ModelWriter() {

  }

  public ModelWriter(String subDir) {
    this.subDir = subDir;
  }

  @Deprecated
  public void writeModel(Model model, String format, String outputFile) throws IOException {
    init(format, outputFile);
    accept(model);
  }


  public void init(String format, String outputFile) {
    this.format = format;
    this.outputFile = outputFile;
  }

  @Override
  public void accept(Model model) {
    try {
      if (!subDir.isEmpty()) {
        outputFile = "./" + subDir + "/" + outputFile;
      }
      logger.info("Saving dataset to " + outputFile + "...");
      long starTime = System.currentTimeMillis();
      FileWriter fileWriter = new FileWriter(outputFile);
      model.write(fileWriter, format);
      logger.info("Saving file done in " + (System.currentTimeMillis() - starTime) + "ms.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}