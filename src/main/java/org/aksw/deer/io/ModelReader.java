package org.aksw.deer.io;

import java.io.InputStream;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.atlas.web.HttpException;
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

  public Model readModel(String fileNameOrUri) {
    final long startTime = System.currentTimeMillis();
    Model model = ModelFactory.createDefaultModel();
    try {
      model.read(fileNameOrUri);
      logger.info(
        "Loading " + fileNameOrUri + " is done in " +
          (System.currentTimeMillis() - startTime) + "ms.");
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTP problem trying to load model from " +
        fileNameOrUri ,e);
    }
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

  public static void main(String[] args) {
    Model m = ModelFactory.createDefaultModel().read("./examples/demo.ttl");
    m.listStatements().forEachRemaining(s->System.out.println(s.toString()));

  }
}
