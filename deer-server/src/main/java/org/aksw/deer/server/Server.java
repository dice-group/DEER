package org.aksw.deer.server;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.MDC;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static spark.Spark.post;
import static spark.Spark.threadPool;
import static spark.Spark.get;
import static spark.Spark.awaitInitialization;

/**
 */
public class Server {

  private static final String STORAGE_DIR_PATH = "./temp/";
  private static final String CONFIG_FILE_PREFIX = "deer_cfg_";
  private static final String CONFIG_FILE_SUFFIX = ".ttl";
  private static Map<String, InputStream> logStreamMap = new HashMap<>();

  public static void main(String[] args) {
    threadPool(4, 1, 30000);
    final String pathToJar = args[0];
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
            pathToJar,
            tempFile.toFile().getAbsolutePath()
          )
          .directory(workingDir)
          .redirectErrorStream(true).start();
        logStreamMap.put(id, process.getInputStream());
        return id;
      } catch (Exception e) {
        e.printStackTrace();
        return e.getMessage();
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

  public static void run(int port) {
    CompletableFuture<Void> x = new CompletableFuture<>();
    CompletableFuture<Void> y = x.thenAcceptAsync($ -> {
      MDC.put("requestId", "jf98hf93h923hf");
      DeerController.runDeer("examples/demo.ttl");
    });
    CompletableFuture<Void> z = x.thenAcceptAsync($->{
      MDC.put("requestId", "k239fb9238fb93");
      DeerController.runDeer("examples/demo.ttl");
    });
    x.complete(null);
    DeerController.runDeer("examples/demo.ttl");
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

}
