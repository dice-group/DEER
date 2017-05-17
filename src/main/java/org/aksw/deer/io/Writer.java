/**
 *
 */
package org.aksw.deer.io;

import java.io.FileWriter;
import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
public class Writer {

  private static final Logger logger = Logger.getLogger(Writer.class.getName());

  private String subDir = "";

  public Writer() {

  }

  public Writer(String subDir) {
    this.subDir = subDir;
  }

  public void writeModel(Model model, String format, String outputFile) throws IOException {
    if (!subDir.isEmpty()) {
      outputFile = "./" + subDir + "/" + outputFile;
    }
    logger.info("Saving dataset to " + outputFile + "...");
    long starTime = System.currentTimeMillis();
    FileWriter fileWriter = new FileWriter(outputFile);
    model.write(fileWriter, format);
    logger.info("Saving file done in " + (System.currentTimeMillis() - starTime) + "ms.");
  }
}