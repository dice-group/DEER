package org.aksw.deer.server;

import eu.medsea.mimeutil.MimeUtil;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.DeerController;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * @author Kevin Dreßler
 */
public class Server {

    private static final String STORAGE_DIR_PATH = "./temp/";
    private static AtomicBoolean busy = new AtomicBoolean(false);
    private static final String CONFIG_FILE_PREFIX = "deer_cfg_";
    private static final String CONFIG_FILE_SUFFIX = ".ttl";



    public static void main(String[] args) {
//        threadPool(4, 1, 30000);
        File uploadDir = new File(STORAGE_DIR_PATH);
        uploadDir.mkdir();
        post("/submit", (req, res) -> {
            if (busy.get()) {
                res.status(409);
                return "Another request is already in progress. Please wait for it to finish.";
            }
            busy.set(true);
            try {
                Path tempFile = Files.createTempFile(uploadDir.toPath(), CONFIG_FILE_PREFIX, CONFIG_FILE_SUFFIX);
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                try (InputStream is = req.raw().getPart("config_file").getInputStream()) {
                    Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                }
                logInfo(req, tempFile);
                String id = tempFile.toString();
                id = id.substring(id.indexOf(CONFIG_FILE_PREFIX) + CONFIG_FILE_PREFIX.length(), id.lastIndexOf(CONFIG_FILE_SUFFIX));
                Writer.setSubDir(id + "/");
                DeerController.run((String[]) Arrays.asList(tempFile.toString(), id).toArray());
                busy.set(false);
                return id;
            } catch (Exception e) {
                busy.set(false);
                throw e;
            }
        });
        get("/result/:id/:file", (req, res) -> {
            File requestedFile = new File(STORAGE_DIR_PATH + req.params("id") + "/" + req.params("file"));
            // is the file available?
            if (requestedFile.exists()) {
                MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
                Collection mimeTypes = MimeUtil.getMimeTypes(requestedFile, new eu.medsea.mimeutil.MimeType("text/plain"));
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

    }

    // methods used for logging
    private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
        System.out.println("Uploaded file '" + getFileName(req.raw().getPart("config_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'");
    }

    private static String getFileName(Part part) {
        System.out.println("123");

        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

}
