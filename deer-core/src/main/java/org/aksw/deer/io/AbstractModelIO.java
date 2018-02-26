package org.aksw.deer.io;

import org.aksw.deer.ParametrizedDeerPlugin;
import org.aksw.faraday_cage.Vocabulary;
import org.aksw.faraday_cage.nodes.AbstractParametrizedNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 *
 *
 *
 */
public abstract class AbstractModelIO extends AbstractParametrizedNode.WithImplicitCloning<Model> implements ParametrizedDeerPlugin {

  private static Supplier<String> workingDirectorySupplier = () -> "";

  public static void takeWorkingDirectoryFrom(Supplier<String> supplier) {
    if (supplier != null) {
      workingDirectorySupplier = supplier;
    }
  }

  protected final String injectWorkingDirectory(String locator) {
    boolean isFile = false;
    try {
      new URL(locator);
    } catch (MalformedURLException ignored) {
      isFile = true;
    }
    if (isFile) {
      Path currentDir = Paths.get(".");
      Path filePath = Paths.get(locator);
      if (filePath.isAbsolute()) {
        filePath = filePath.getFileName();
      }
      if (workingDirectorySupplier != null) {
        locator = currentDir.resolve(workingDirectorySupplier.get()).resolve(filePath).normalize().toString();
      }
    }
    return locator;
  }


  @Override
  protected Model deepCopy(Model data) {
    return ModelFactory.createDefaultModel().add(data);
  }

  @NotNull
  @Override
  public Resource getType() {
    return Vocabulary.resource(this.getClass().getSimpleName());
  }
}
