package org.aksw.deer.server;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.aksw.deer.io.WorkingDirectoryInjectedIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static spark.Spark.*;

/**
 */
public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private static final String STORAGE_DIR_PATH = "./.server-storage/";
  private static final String LOG_DIR_PATH = STORAGE_DIR_PATH + "logs/";
  private static final String CONFIG_FILE_PREFIX = "deer_cfg_";
  private static final String CONFIG_FILE_SUFFIX = ".ttl";
  private static Map<String, CompletableFuture<Void>> requests = new HashMap<>();

  public static void run(int port) {
    File uploadDir = new File(STORAGE_DIR_PATH);
    if (!uploadDir.exists()) {
      uploadDir.mkdir();
    }
    WorkingDirectoryInjectedIO.takeWorkingDirectoryFrom(()-> STORAGE_DIR_PATH + MDC.get("requestId") + "/");
    threadPool(8, 1, 30000);
    port(port);
    init();
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
        final String requestId = id;
        requests.put(requestId, CompletableFuture.completedFuture(null).thenAcceptAsync($->{
          MDC.put("requestId", requestId);
          DeerController.runDeer(tempFile.toString());
        }));
        return id;
      } catch (Exception e) {
        e.printStackTrace();
        return e.getMessage();
      }
    });
    get("/result/:id", (req, res) -> {
      String id = sanitizeId(req.params("id"));
      File dir = new File(STORAGE_DIR_PATH + id);
      if (dir.exists() && dir.isDirectory()) {
        return Arrays.stream(dir.listFiles())
          .map(File::getName)
          .reduce("", (s, s2) -> s + ", " + s2)
          .substring(2);
      } else {
        // 404 - Not Found
        res.status(404);
        return "404 - request id not found";
      }
    });
    get("/result/:id/:file", (req, res) -> {
      String id = sanitizeId(req.params("id"));
      File file = new File(req.params("file"));
      File requestedFile = new File(STORAGE_DIR_PATH + id + "/" + file.getName());
      // is the file available?
      if (requestedFile.exists()) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        Collection mimeTypes = MimeUtil.getMimeTypes(requestedFile, new MimeType("text/plain"));
        res.type(mimeTypes.iterator().next().toString());
        res.header("Content-Disposition", "attachment; filename=" + file.getName());
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
    get("/logs/:id", (req, res) -> {
      String id = sanitizeId(req.params("id"));
      File requestedFile = new File(LOG_DIR_PATH + id + ".log");
      // is the file available?
      if (requestedFile.exists()) {
        res.type("text/plain");
        res.header("Content-Disposition", "attachment; filename=log.txt");
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
        return "404 - log file not found";
      }
    });
    awaitInitialization();
  }

  private static String sanitizeId(String id) {
    return id.replaceAll("[^\\d]", "");
  }

  // methods used for logging
  private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
    logger.info("Uploaded file '{}' saved as '{}'", getFileName(req.raw().getPart("config_file")),
      tempFile.toAbsolutePath());
  }

  private static String getFileName(Part part) {
    for (String cd : part.getHeader("content-disposition").split(";")) {
      if (cd.trim().startsWith("filename")) {
        return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return null;
  }

}
