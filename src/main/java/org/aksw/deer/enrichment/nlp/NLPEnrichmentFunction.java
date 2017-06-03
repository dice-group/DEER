package org.aksw.deer.enrichment.nlp;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.deer.vocabulary.DBpedia;
import org.aksw.deer.vocabulary.SCMSANN;
import org.aksw.deer.vocabulary.SPECS;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.util.ParameterType;
import org.aksw.deer.enrichment.AEnrichmentFunction;
import org.aksw.fox.binding.java.FoxApi;
import org.aksw.fox.binding.java.FoxParameter.OUTPUT;
import org.aksw.fox.binding.java.FoxResponse;
import org.aksw.fox.binding.java.IFoxApi;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import ro.fortsoft.pf4j.Extension;

//import org.apache.commons.io.IOUtils;
//import org.junit.Test;

/**
 * @author sherif
 */
@Extension
public class NLPEnrichmentFunction extends AEnrichmentFunction {

  public static final String ORGANIZATION = "organization";
  public static final String LOCATION = "location";
  public static final String PERSON = "person";
//    private static String FOX_SERVICE_URI = "http://fox-demo.aksw.org/call/ner/entities";
//	private static String FOX_SERVICE_URI = "http://sake.informatik.uni-leipzig.de:4444/call/ner/entities";
  public static final String ALL = "all";
  public static final String LITERAL_PROPERTY_DESC =
    "Literal property used by FOX for NER. " +
      "If not set, the top ranked literal property will be pecked";
  public static final String ADDED_PROPERTY_DESC =
    "Property added to the input model with additional Geospatial " +
      "knowledge through NLP. By default, " +
      "this parameter is set to 'gn:relatedTo'";
  public static final String USE_FOX_LIGHT_DESC =
    "An implemented NER class name. " +
      "By default this parameter is set to 'OFF' " +
      "in which all NER classes run in parallel " +
      "and a combined result will be returned. " +
      "If this parameter is given with a wrong value, " +
      "'NERStanford' will be used";
  public static final String USE_FOX_LIGHT_VALUES =
    "OFF, org.aksw.fox.nertools.NEROpenNLP," +
      "org.aksw.fox.nertools.NERIllinoisExtended," +
      "org.aksw.fox.nertools.NERIllinoisExtended," +
      "org.aksw.fox.nertools.NERBalie," +
      "org.aksw.fox.nertools.NERStanford";
  public static final String ASK_END_POINT_DESC =
    "Ask the DBpedia endpoint for each location returned by FOX " +
      "(setting it generates slower execution time but more accurate results). " +
      "By default this parameter is set to 'false'";
  public static final String NER_TYPE_DESC =
    "Force FOX to look for a specific NE’s types only. ";
  public static final String NER_TYPE_VALUES =
    LOCATION + "," + ORGANIZATION + "," + PERSON + "," + ALL;
  public static final String ASK_END_POINT = "askEndPoint";
  public static final String ADDED_PROPERTY = "addedProperty";
  public static final String NER_TYPE = "NERType";
  public static final String USE_FOX_LIGHT = "useFoxLight";
  public static final String LITERAL_PROPERTY = "literalProperty";
  public static final String FOX_API_URL = "http://139.18.2.164:4444/api";
  public static final String DBPEDIA_END_POINT = "dbpediaendpoint";
  private static final Logger logger = Logger.getLogger(NLPEnrichmentFunction.class.getName());
  private static String dbpediaEndPoint = "http://dbpedia.org/sparql";
  //@todo: this should be configurable
  private static String FOX_SERVICE_URI = "http://fox:4444/call/ner/entities";
  private static String NEType = LOCATION;
  // parameters list
  private Property literalProperty;
  private String useFoxLight = "OFF"; //"org.aksw.fox.nertools.NERStanford"; ;
  private boolean askEndPoint = false;
  private String foxType = "TEXT";
  private String foxTask = "NER";
  private String foxOutput = "Turtle";
  private boolean foxUseNif = false;
  private boolean foxReturnHtml = false;
  private String inputFile = "";
  private String outputFile = "";
  private Property addedProperty = ResourceFactory
    .createProperty("http://geoknow.org/ontology/relatedTo");

  public NLPEnrichmentFunction() {
    super();
  }

  /**
   * @return the relatedToProperty
   */
  public Property getRelatedToProperty() {
    return addedProperty;
  }

  /**
   * @param relatedToProperty the relatedToProperty to set
   */
  public void setRelatedToProperty(Property relatedToProperty) {
    this.addedProperty = relatedToProperty;
  }

  /**
   * @return the model
   */
  public Model getModel() {
    return model;
  }

  /**
   * @param model the model to setModel
   */
  public void setModel(Model model) {
    this.model = model;
  }

  /**
   * @return the literalProperty
   */
  public Property getliteralProperty() {
    return literalProperty;
  }

  /**
   * @param p the literalProperty to set
   */
  public void setliteralProperty(Property p) {
    this.literalProperty = p;
  }

  public Model getNamedEntityModel(String inputText) {
    String buffer = getNamedEntity(foxType, foxTask, foxOutput, inputText, useFoxLight, foxUseNif,
      foxReturnHtml);
    ByteArrayInputStream stream = null;
    try {
      stream = new ByteArrayInputStream(buffer.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    Model NamedEntitymodel = ModelFactory.createDefaultModel();
    if (buffer.contains("<!--")) {
      return NamedEntitymodel;
    }
    NamedEntitymodel.read(stream, "", "TTL");
    return NamedEntitymodel;
  }

  public String refineString(String inputString) {
    String outputString = inputString;
    outputString.replace("<", "").replace(">", "").replace("//", "");
    return outputString;
  }

  /**
   * @param type: text or an url (e.g.: `G. W. Leibniz was born in Leipzig`,
   * `http://en.wikipedia.org/wiki/Leipzig_University`)
   * @param task: { NER }
   * @param output: { JSON-LD | N-Triples | RDF/{ JSON | XML } | Turtle | TriG | N-Quads}
   * @param input: text or an url
   * @param foxlight: an implemented INER class name (e.g.: `org.aksw.fox.nertools.NEROpenNLP`) or
   * `OFF`. org.aksw.fox.nertools.NERIllinoisExtended org.aksw.fox.nertools.NEROpenNLP
   * org.aksw.fox.nertools.NERBalie org.aksw.fox.nertools.NERStanford
   * @param nif: { true | false }
   * @param returnHtml: { true | false }
   * @return Named entity buffer containing annotation of the input text
   * @author sherif
   */
  @SuppressWarnings("deprecation")
  private String getNamedEntity(String type, String task, String output, String input,
    String foxlight, boolean nif, boolean returnHtml) {
    String buffer = "", line;
    boolean error = true;
    while (error) {
      try {
        input = refineString(input);
        // Construct data
        String data = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8");
        data += "&" + URLEncoder.encode("task", "UTF-8") + "=" + URLEncoder.encode(task, "UTF-8");
        data +=
          "&" + URLEncoder.encode("output", "UTF-8") + "=" + URLEncoder.encode(output, "UTF-8");
        data += "&" + URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(input, "UTF-8");
        data +=
          "&" + URLEncoder.encode("foxlight", "UTF-8") + "=" + URLEncoder.encode(foxlight, "UTF-8");
        data += "&" + URLEncoder.encode("nif", "UTF-8") + "=" + URLEncoder
          .encode((nif) ? "TRUE" : "FALSE", "UTF-8");
        data += "&" + URLEncoder.encode("returnHtml", "UTF-8") + "=" + URLEncoder
          .encode((returnHtml) ? "TRUE" : "FALSE", "UTF-8");

        // Send data
        URL url = new URL(FOX_API_URL);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while ((line = rd.readLine()) != null) {
          buffer = buffer + line + "\n";
        }
        wr.close();
        rd.close();
        error = false;
      } catch (Exception e) {
        logger.error("FOX Exception: " + e);
        e.printStackTrace();
      }
    }

    //TODO use a JASON parser
    buffer = URLDecoder.decode(buffer);
    buffer = buffer.substring(buffer.indexOf("@"), buffer.lastIndexOf("log") - 4);

    return buffer;
  }

  /**
   * @return model of places contained in the input model
   * @author sherif
   */
  public Model getNE(Model namedEntityModel, RDFNode subject, Resource type) {

    Model resultModel = ModelFactory.createDefaultModel();
    String sparqlQueryString = "CONSTRUCT {?s ?p ?o} " +
      " WHERE {?s a <" + type.toString() + ">. ?s ?p ?o} ";
    QueryFactory.create(sparqlQueryString);
    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, namedEntityModel);
    Model locationsModel = qexec.execConstruct();
    Property meansProperty = ResourceFactory.createProperty("http://ns.aksw.org/scms/means");
    NodeIterator objectsIter = locationsModel.listObjectsOfProperty(meansProperty);
    if (askEndPoint) {
      while (objectsIter.hasNext()) {
        RDFNode object = objectsIter.nextNode();
        if (object.isResource()) {
          if (isPlace(object)) {
            resultModel.add((Resource) subject, addedProperty, object);
            //					TODO add more data ??
            logger.info("<" + subject.toString() + "> <" + addedProperty + "> <" + object + ">");
          }
        }
      }
    } else {
      while (objectsIter.hasNext()) {
        RDFNode object = objectsIter.nextNode();
        if (object.isResource()) {

          resultModel.add((Resource) subject, addedProperty, object);
          //					TODO add more data ??
          logger.info("<" + subject.toString() + "> <" + addedProperty + "> <" + object + ">");
        }
      }
    }
    return resultModel;
  }

  /**
   * As a generalization of GeoLift
   *
   * @return model of all NEs contained in the input model
   * @author sherif
   */
  public Model getNE(Model namedEntityModel, RDFNode subject) {
    Model resultModel = ModelFactory.createDefaultModel();
    Property meansProperty = ResourceFactory.createProperty("http://ns.aksw.org/scms/means");
    NodeIterator objectsIter = namedEntityModel.listObjectsOfProperty(meansProperty);
    while (objectsIter.hasNext()) {
      RDFNode object = objectsIter.nextNode();
      if (object.isResource()) {
        resultModel.add((Resource) subject, addedProperty, object);
        //					TODO add more data ??
        logger.info("<" + subject.toString() + "> <" + addedProperty + "> <" + object + ">");
      }
    }
    return resultModel;
  }

  /**
   * @return wither is the input URI is a place of not
   * @author sherif
   */
  private boolean isPlace(RDFNode uri) {
    boolean result = false;
    if (uri.toString().contains("http://ns.aksw.org/scms/")) {
      return false;
    }
    String queryString = "ask {<" + uri.toString() + "> a <http://dbpedia.org/ontology/Place>}";
    logger.info("Asking DBpedia for: " + queryString);
    Query query = QueryFactory.create(queryString);
    //		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
    QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
    result = qexec.execAsk();
    logger.info("Answer: " + result);
    return result;
  }

  /**
   * @return just a TEST
   * @author sherif
   */
  public List<String> getDBpediaAbstaracts(Integer limit) {
    List<String> result = new ArrayList<String>();

    String queryString = "SELECT distinct ?o WHERE {" +
      "?s a <http://dbpedia.org/ontology/Place>." +
      "?s <http://dbpedia.org/ontology/abstract> ?o } LIMIT " + limit.toString();
    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.sparqlService(dbpediaEndPoint, query);
    ResultSet queryResults = qexec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      result.add(qs.getLiteral("o").toString());
    }
    qexec.close();
    return result;
  }

  /**
   * @return Geo-spatial enriched model
   * @author sherif
   */
  public Model getEnrichrdTriples() {
    Model resultModel = ModelFactory.createDefaultModel();
    StmtIterator stItr = model.listStatements(null, literalProperty, (RDFNode) null);
    logger.info("--------------- Added triples through  NLP ---------------");
    while (stItr.hasNext()) {
      Model namedEntityModel = ModelFactory.createDefaultModel();
      Statement st = stItr.nextStatement();
      RDFNode object = st.getObject();
      RDFNode subject = st.getSubject();
      if (object.isLiteral()) {
        namedEntityModel = runFOX(object);
      }
      if (!namedEntityModel.isEmpty()) {
        if (NEType.equalsIgnoreCase("all")) { // Extract all NE (Generalization of GeoLift)
          resultModel.add(getNE(namedEntityModel, subject));
        } else if (NEType.equalsIgnoreCase(LOCATION)) {
          resultModel.add(getNE(namedEntityModel, subject, SCMSANN.LOCATION));
        } else if (NEType.equalsIgnoreCase(PERSON)) {
          resultModel.add(getNE(namedEntityModel, subject, SCMSANN.PERSON));
        } else if (NEType.equalsIgnoreCase(ORGANIZATION)) {
          resultModel.add(getNE(namedEntityModel, subject, SCMSANN.ORGANIZATION));
        }
      }
    }
    resultModel.add(model);
    return resultModel;
  }

  private Model runFOX(RDFNode object) {
    Model namedEntityModel = ModelFactory.createDefaultModel();
    try {
      // request FOX
      Response response = Request
        .Post(FOX_SERVICE_URI)
        .addHeader("Content-type", "application/json")
        .addHeader("Accept-Charset", "UTF-8")
        .body(new StringEntity(new JSONObject()
          .put("input", object.toString())
          .put("type", "text").put("task", "ner")
          .put("output", "TTL").toString(),
          ContentType.APPLICATION_JSON)).execute();
      HttpResponse httpResponse = response.returnResponse();
      HttpEntity entry = httpResponse.getEntity();
      namedEntityModel.read(entry.getContent(), null, "TTL");
      //				System.out.println(IOUtils.toString(entry.getContent()));
      EntityUtils.consume(entry);
    } catch (Exception e) {
      logger.error("Got an exception while communicating with the FOX web service.");
      System.out.println(e);
    }
    return namedEntityModel;
  }

  private Model runFOX_old(RDFNode object) {
    Model namedEntityModel = ModelFactory.createDefaultModel();
    try {
      IFoxApi fox = new FoxApi();
      FoxResponse foxRes = fox
        .setApiURL(new URL(FOX_SERVICE_URI))
        .setInput(object.toString())
        .setOutputFormat(OUTPUT.TURTLE).send();
      namedEntityModel.read(new StringReader(foxRes.getOutput().trim()), null, "TTL");
    } catch (Exception e) {
      logger.error(e);
      logger.error(object.toString());
    }
    return namedEntityModel;
  }

  public Model enrichModel() {
    return model.union(getEnrichrdTriples());
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.enrichment.GeoLiftModule#process(org.apache.jena.rdf.model.Model, java.util.Map)
   */
  public Model process() {
    logger.info("--------------- NLP Module ---------------");
    if (parameters.containsKey("input")) {
      inputFile = parameters.get("input");
      model = (new ModelReader()).readModel(inputFile);
    }
    if (parameters.containsKey(LITERAL_PROPERTY)) {
      literalProperty = ResourceFactory.createProperty(parameters.get(LITERAL_PROPERTY));
    } else {
      LiteralPropertyRanker lpr = new LiteralPropertyRanker(model);
      literalProperty = lpr.getTopRankedLiteralProperty();
      if (literalProperty == null) {
        logger.info("No Literal Properties!, return input model.");
        return model;
      }
      logger.info("Top ranked Literal Property: " + literalProperty);
    }
    if (parameters.containsKey(ADDED_PROPERTY)) {
      addedProperty = ResourceFactory.createProperty(ADDED_PROPERTY);
    }
    if (parameters.containsKey(USE_FOX_LIGHT)) {
      useFoxLight = parameters.get(USE_FOX_LIGHT).toLowerCase();
    }
    if (parameters.containsKey(ASK_END_POINT)) {
      askEndPoint = parameters.get(ASK_END_POINT).toLowerCase().equals("true");
    }
    //		if( parameters.containsKey("foxType"))
    //			foxType = parameters.get("foxType").toUpperCase();
    //		if( parameters.containsKey("foxTask"))
    //			foxTask = parameters.get("foxTask").toUpperCase();
    //		if( parameters.containsKey("foxInput"))
    //			foxInput = parameters.get("foxInput");
    //		if( parameters.containsKey("foxOutput"))
    //			foxOutput = parameters.get("foxOutput");
    //		if( parameters.containsKey("foxUseNif"))
    //			foxUseNif = parameters.get("foxUseNif").toLowerCase().equals("true")? true : false;
    //		if( parameters.containsKey("foxReturnHtml"))
    //			foxReturnHtml = parameters.get("foxReturnHtml").toLowerCase().equals("true")? true : false;
    //		if( parameters.containsKey("extractAllNE"))
    //			foxReturnHtml = parameters.get("extractAllNE").toLowerCase().equals("true")? true : false;
    if (parameters.containsKey(NER_TYPE)) {
      NEType = parameters.get(NER_TYPE).toLowerCase();
    }
    if (parameters.containsKey(DBPEDIA_END_POINT)) {
      dbpediaEndPoint = parameters.get(DBPEDIA_END_POINT).toLowerCase();
    }

    Model enrichedModel = getEnrichrdTriples();
    enrichedModel.add(model);

    if (parameters.containsKey("output")) {
      outputFile = parameters.get("output");
      FileWriter outFile = null;
      try {
        outFile = new FileWriter(outputFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      enrichedModel.write(outFile, "TURTLE");
    }
    return enrichedModel;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.enrichment.GeoLiftModule#getParameters()
   */
  public List<String> getParameters() {
    List<String> parameters = new ArrayList<String>();
    //		parameters.add("input");
    //		parameters.add("output");
    parameters.add(LITERAL_PROPERTY);
    parameters.add(USE_FOX_LIGHT);
    parameters.add(ASK_END_POINT);
    //		parameters.add("foxType");
    //		parameters.add("foxTask");
    //		parameters.add("foxInput");
    //		parameters.add("foxOutput");
    //		parameters.add("foxUseNif");
    //		parameters.add("foxReturnHtml");
    parameters.add(ADDED_PROPERTY);
    parameters.add(NER_TYPE);
    return parameters;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.enrichment.GeoLiftModule#getNecessaryParameters()
   */
  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<String>();
    return parameters;
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  /**
   * Self configuration
   * Set all parameters to default values, also extract all NEs
   *
   * @return Map of (key, value) pairs of self configured parameters
   * @author sherif
   */
  public Map<String, String> selfConfig(Model source, Model target) {

    //		Set<Resource> uriObjects = getDiffUriObjects(source, target);

    Map<String, String> p = new HashMap<String, String>();
    p.put(NER_TYPE, ALL);
    return p;
  }

  @Override
  public List<ParameterType> getParameterWithTypes() {
    List<ParameterType> parameters = new ArrayList<ParameterType>();
    parameters
      .add(new ParameterType(ParameterType.STRING, LITERAL_PROPERTY, LITERAL_PROPERTY_DESC, false));
    parameters.add(new ParameterType(ParameterType.STRING, ADDED_PROPERTY, USE_FOX_LIGHT_VALUES,
      ADDED_PROPERTY_DESC, false));
    parameters
      .add(new ParameterType(ParameterType.STRING, USE_FOX_LIGHT, USE_FOX_LIGHT_DESC, false));
    parameters
      .add(new ParameterType(ParameterType.BOOLEAN, ASK_END_POINT, ASK_END_POINT_DESC, false));
    parameters.add(
      new ParameterType(ParameterType.STRING, NER_TYPE, NER_TYPE_VALUES, NER_TYPE_DESC, false));
    return parameters;
  }

  @Override
  public Resource getType() {
    return SPECS.NLPModule;
  }

}
