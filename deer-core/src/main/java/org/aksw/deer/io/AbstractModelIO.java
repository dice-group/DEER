package org.aksw.deer.io;

import org.aksw.deer.ParameterizedDeerExecutionNode;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 *
 *
 *
 */
public abstract class AbstractModelIO extends AbstractParameterizedExecutionNode.WithImplicitCloning<Model> implements ParameterizedDeerExecutionNode {

  private static Supplier<String> workingDirectorySupplier = () -> "";


  /**
   * Specify the supplier of the working directory injection.
   * It <b>must</b> have the necessary information to construct a valid
   * working directory path at ExecutionGraph compileCanonicalForm time.
   * @param supplier the supplier
   */
  public static void takeWorkingDirectoryFrom(Supplier<String> supplier) {
    if (supplier != null) {
      workingDirectorySupplier = supplier;
    }
  }

  /**
   * Use this always when writing to or reading from Files whose paths are specified by parameters.
   * @param pathString Path to inject working directory into
   * @return injected path
   */
  public static String injectWorkingDirectory(String pathString) {
      Path path = Paths.get(pathString);
      Path currentDir = Paths.get(".");
      if (workingDirectorySupplier != null && path.isAbsolute()) {
        path = path.getFileName();
      }
      if (workingDirectorySupplier != null) {
        path = currentDir.resolve(workingDirectorySupplier.get()).resolve(path).normalize();
      }
    return path.toString();
  }

  @Override
  public Model deepCopy(Model data) {
    return ModelFactory.createDefaultModel().add(data);
  }

  @Override
  public Resource getType() {
    return DEER.resource(this.getClass().getSimpleName());
  }

//  @Override
//  protected void writeInputAnalytics(List<Model> data) {
//    if (getInDegree() > 0) {
//      writeAnalytics("input sizes", data.stream().map(m -> String.valueOf(m.size())).reduce("( ", (a, b) -> a + b + " ") + ")");
//    }
//  }
//
//  @Override
//  protected void writeOutputAnalytics(List<Model> data) {
//    if (getOutDegree() > 0) {
//      writeAnalytics("output sizes", data.stream().map(m->String.valueOf(m.size())).reduce("( ", (a, b) -> a + b + " ") + ")");
//    }
//  }

}
