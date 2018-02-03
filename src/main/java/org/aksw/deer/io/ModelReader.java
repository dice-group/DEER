package org.aksw.deer.io;

import java.io.InputStream;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;

/**
 *
 */
public class ModelReader {

  private static final Logger logger = Logger.getLogger(ModelReader.class);

  private String subDir = "";

  public ModelReader() {

  }

  public ModelReader(String subDir) {
    this.subDir = subDir;
  }

  public Model readModel(String fileNameOrUri) {
    return readModel(fileNameOrUri, false);
  }

  private Model readModel(String fileNameOrUri, boolean ignoreSubDir) {
    if (!subDir.isEmpty() && !ignoreSubDir) {
      try {
        return readModel("./" + subDir + "/" + fileNameOrUri, true);
      } catch (Exception e) {
        logger.debug("Ignoring subdirectory setting for input file: " + fileNameOrUri);
      }
    }
    long startTime = System.currentTimeMillis();
    Model model = ModelFactory.createDefaultModel();
    InputStream in = FileManager.get().open(fileNameOrUri);
    if (in == null) {
      throw new IllegalArgumentException(fileNameOrUri + " not found");
    }
    if (fileNameOrUri.contains(".ttl") || fileNameOrUri.contains(".n3")) {
      logger.info("Opening Turtle file");
      model.read(in, null, "TTL");
    } else if (fileNameOrUri.contains(".rdf")) {
      logger.info("Opening RDFXML file");
      model.read(in, null);
    } else if (fileNameOrUri.contains(".nt")) {
      logger.info("Opening N-Triples file");
      model.read(in, null, "N-TRIPLE");
    } else {
      logger.info("Content negotiation to get RDFXML from " + fileNameOrUri);

      model.read(fileNameOrUri);
    }
    logger.info(
      "Loading " + fileNameOrUri + " is done in " + (System.currentTimeMillis() - startTime)
        + "ms.");
    return model;
  }


  /**
   * @param endpointUri
   * @param dataset
   * @return
   */
  public static Model readModelFromEndPoint(Resource dataset, String endpointUri) {
    //@todo: implement new parameter for content type
    //@todo refactor for better tests
    Model result;
    long startTime = System.currentTimeMillis();
    logger.info("Reading dataset  " + dataset + " from " + endpointUri);
    Statement fromGraph = dataset.getProperty(DEER.fromGraph);
    Statement hasUri = dataset.getProperty(DEER.hasUri);
    if (fromGraph != null) {
      String triplePattern = "?s ?p ?o";
      Statement graphTriplePattern = dataset.getProperty(DEER.graphTriplePattern);
      if (graphTriplePattern != null) {
        triplePattern = graphTriplePattern.getObject().toString();
      }
      String sparqlQueryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <"
        + fromGraph.getObject().toString() + "> { " + triplePattern + " } . }";
      QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri, sparqlQueryString);
      result = qExec.execConstruct();
      qExec.close();
    } else if (hasUri != null) {
      String sparqlQueryString = "DESCRIBE <" + hasUri.getObject().toString() + ">";
      QueryEngineHTTP qExec = new QueryEngineHTTP(endpointUri, sparqlQueryString);
      qExec.setModelContentType(WebContent.contentTypeJSONLD);
      result = qExec.execDescribe();
      qExec.close();
    } else {
      throw new RuntimeException("Neither " + DEER.hasUri + " nor " + DEER.fromGraph +
        " defined to generate dataset " + dataset + " from " + endpointUri
        + ", exit with error.");
    }
    logger.info("Dataset reading is done in " + (System.currentTimeMillis() - startTime) + "ms, " + result.size() + " triples found.");
    return result;
  }

}
