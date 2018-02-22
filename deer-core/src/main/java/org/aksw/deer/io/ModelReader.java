package org.aksw.deer.io;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 *
 */
public class ModelReader extends WorkingDirectoryInjectedIO {

  private static final Logger logger = LoggerFactory.getLogger(ModelReader.class);

  public Model readModel(String locator) {
    locator = injectWorkingDirectory(locator);
    try {
      Model model = ModelFactory.createDefaultModel();
      final long startTime = System.currentTimeMillis();
      model.read(locator);
      logger.info("Loading {} is done in {}ms.", locator,
        (System.currentTimeMillis() - startTime));
      return model;
    } catch (HttpException e) {
      throw new RuntimeException("Encountered HTTPException trying to load model from " +
        locator, e);
    }
  }

  public Model readModelFromEndPoint(Resource dataset, String endpointUri) {
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
