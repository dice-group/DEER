package org.aksw.deer.server;

import static spark.Spark.*;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.Request;

/**
 * @author Kevin Dreßler
 */
public class Server {

  private static final String STORAGE_DIR_PATH = "./temp/";
  private static final String CONFIG_FILE_PREFIX = "deer_cfg_";
  private static final String CONFIG_FILE_SUFFIX = ".ttl";
  private static Map<String, InputStream> logStreamMap;

  public static void main(String[] args) {
    threadPool(4, 1, 30000);
    File uploadDir = new File(STORAGE_DIR_PATH);
    uploadDir.mkdir();

//    webSocket("/log", LogSocket.class);

    post("/submit", (req, res) -> {
      try {
        Path tempFile = Files
          .createTempFile(uploadDir.toPath(), CONFIG_FILE_PREFIX, CONFIG_FILE_SUFFIX);
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        try (InputStream is = req.raw().getPart("config_file").getInputStream()) {
          Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        logInfo(req, tempFile);
        String id = tempFile.toString();
        id = id.substring(id.indexOf(CONFIG_FILE_PREFIX) + CONFIG_FILE_PREFIX.length(),
          id.lastIndexOf(CONFIG_FILE_SUFFIX));
        File workingDir = new File(uploadDir.getAbsoluteFile(), id);
        if (!workingDir.mkdir()) {
          throw new RuntimeException("Not able to create directory " + workingDir.getAbsolutePath());
        }
        Process process = new ProcessBuilder()
          .command(
          "java",
          "-jar",
          urlToFile(getLocation(Server.class)).getAbsolutePath(),
          tempFile.toFile().getAbsolutePath()
        )
          .directory(workingDir)
          .redirectErrorStream(true)
          .start();
        logStreamMap.put(id, process.getInputStream());
        return id;
      } catch (Exception e) {
        return Arrays.toString(e.getStackTrace());
      }
    });

    get("/result/:id/:file", (req, res) -> {
      File requestedFile = new File(STORAGE_DIR_PATH + req.params("id") + "/" + req.params("file"));
      // is the file available?
      if (requestedFile.exists()) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        Collection mimeTypes = MimeUtil.getMimeTypes(requestedFile, new MimeType("text/plain"));
        res.type(mimeTypes.iterator().next().toString());
        res.header("Content-Disposition", "attachment; filename=" + req.params("file"));
        res.status(200);
        OutputStream os = res.raw().getOutputStream();
        FileInputStream fs = new FileInputStream(requestedFile);
        final byte[] buffer = new byte[1024];
        int count;
        while ((count = fs.read(buffer)) >= 0) {
          os.write(buffer, 0, count);
        }
        os.flush();
        fs.close();
        os.close();
        return "";
      } else {
        // 404 - Not Found
        res.status(404);
        return "404 - file not found";
      }
    });

    awaitInitialization();
  }
  @WebSocket
  public static class LogSocket {

    private InputStream inputStream;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
      Map<String, List<String>> parameterMap = user.getUpgradeRequest().getParameterMap();
      inputStream = logStreamMap.get(parameterMap.get("id").get(0));
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 1024);
      user.getRemote().sendBytesByFuture(byteBuffer);

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {

    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {

    }


  }

  // methods used for logging
  private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
    System.out.println(
      "Uploaded file '" + getFileName(req.raw().getPart("config_file")) + "' saved as '" +
        tempFile.toAbsolutePath() + "'");
  }

  private static String getFileName(Part part) {
    for (String cd : part.getHeader("content-disposition").split(";")) {
      if (cd.trim().startsWith("filename")) {
        return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return null;
  }


  /**
   * Gets the base location of the given class.
   * <p>
   * If the class is directly on the file system (e.g.,
   * "/path/to/my/package/MyClass.class") then it will return the base directory
   * (e.g., "file:/path/to").
   * </p>
   * <p>
   * If the class is within a JAR file (e.g.,
   * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
   * path to the JAR (e.g., "file:/path/to/my-jar.jar").
   * </p>
   *
   * @param c The class whose location is desired.
   * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
   */
  public static URL getLocation(final Class<?> c) {
    if (c == null) return null; // could not load the class

    // try the easy way first
    try {
      final URL codeSourceLocation =
        c.getProtectionDomain().getCodeSource().getLocation();
      if (codeSourceLocation != null) return codeSourceLocation;
    }
    catch (final SecurityException e) {
      // NB: Cannot access protection domain.
    }
    catch (final NullPointerException e) {
      // NB: Protection domain or code source is null.
    }

    // NB: The easy way failed, so we try the hard way. We ask for the class
    // itself as a resource, then strip the class's path from the URL string,
    // leaving the base path.

    // get the class's raw resource path
    final URL classResource = c.getResource(c.getSimpleName() + ".class");
    if (classResource == null) return null; // cannot find class resource

    final String url = classResource.toString();
    final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
    if (!url.endsWith(suffix)) return null; // weird URL

    // strip the class's path from the URL string
    final String base = url.substring(0, url.length() - suffix.length());

    String path = base;

    // remove the "jar:" prefix and "!/" suffix, if present
    if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

    try {
      return new URL(path);
    }
    catch (final MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }


  /**
   * Converts the given {@link URL} to its corresponding {@link File}.
   * <p>
   * This method is similar to calling {@code new File(url.toURI())} except that
   * it also handles "jar:file:" URLs, returning the path to the JAR file.
   * </p>
   *
   * @param url The URL to convert.
   * @return A file path suitable for use with e.g. {@link FileInputStream}
   * @throws IllegalArgumentException if the URL does not correspond to a file.
   */
  public static File urlToFile(final URL url) {
    return url == null ? null : urlToFile(url.toString());
  }

  /**
   * Converts the given {@link URL} to its corresponding {@link File}.
   * <p>
   * This method is similar to calling {@code new File(url.toURI())} except that
   * it also handles "jar:file:" URLs, returning the path to the JAR file.
   * </p>
   *
   * @param url The URL to convert.
   * @return A file path suitable for use with e.g. {@link FileInputStream}
   * @throws IllegalArgumentException if the URL does not correspond to a file.
   */
  public static File urlToFile(final String url) {
    String path = url;
    if (path.startsWith("jar:")) {
      // remove "jar:" prefix and "!/" suffix
      final int index = path.indexOf("!/");
      path = path.substring(4, index);
    }
    try {
      return new File(new URL(path).toURI());
    }
    catch (final MalformedURLException e) {
      // NB: URL is not completely well-formed.
    }
    catch (final URISyntaxException e) {
      // NB: URL is not completely well-formed.
    }
    if (path.startsWith("file:")) {
      // pass through the URL as-is, minus "file:" prefix
      path = path.substring(5);
      return new File(path);
    }
    throw new IllegalArgumentException("Invalid URL: " + url);
  }

}
