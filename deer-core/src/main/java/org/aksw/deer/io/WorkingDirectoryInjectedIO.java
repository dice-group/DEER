package org.aksw.deer.io;

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
public abstract class WorkingDirectoryInjectedIO {

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



}
