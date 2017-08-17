package org.aksw.deer.evaluation;

/**
 * @author sherif
 */
public class RandomSpecsGenerator {
//
//  private static final Logger logger = Logger.getLogger(RandomSpecsGenerator.class.getName());
//  public static Model specsModel = ModelFactory.createDefaultModel();
//  private static int datasetIndex = 1;
//  private static ConfigBuilder configBuilder = new ConfigBuilder();
//  private static List<IEnrichmentOperator> deerModules = new PluginFactory<>(IEnrichmentOperator.class).getImplementations();
//  private static List<IEnrichmentOperator> deerOperators = new PluginFactory<>(IEnrichmentOperator.class).getImplementations();
//
//
//  /**
//   * @param size (number of enrichment included in the resulted configuration)
//   * @param complexity [0,1], 0 means only enrichment, 1 means only operator
//   * @return a random configuration file
//   * @author sherif
//   */
//  public static Model generateSpecs(String inputDataFile, String outputDataFile, int size,
//    double complexity) {
//    Model specsModel = generateSpecs(inputDataFile, size, complexity);
//    Resource finalDataset = ConfigAnalyzer.getFinalDatasets(specsModel).get(0);
//    specsModel.add(finalDataset, DEER.outputFile, outputDataFile);
//    return specsModel;
//  }
//
//  /**
//   * @param size (number of enrichment included in the resulted configuration)
//   * @param complexity [0,1], 0 means only enrichment, 1 means only operator
//   * @return a random configuration file
//   * @author sherif
//   */
//  public static Model generateSpecs(String inputDataFile, int size, double complexity) {
//    Model inputDataModel = new ModelReader().readModel(inputDataFile);
//    Model specsModel = generateSpecs(inputDataModel, size, complexity);
//    Resource firstDataset = ResourceFactory.createResource(DEER.uri + "dataset_1");
//    configBuilder.addDataset(specsModel, firstDataset, inputDataFile);
//    return specsModel;
//  }
//
//  /**
//   * @param size (number of enrichment included in the resulted configuration)
//   * @param complexity [0,1], 0 means only enrichment, 1 means only operator
//   * @return a random configuration file with complexity
//   * @author sherif
//   */
//  public static Model generateSpecs(Model inputDataModel, int size, double complexity) {
//    if (complexity < 0 || complexity >= 1) {
//      logger.error("Specs complexity must be in [0,1[");
//      System.exit(1);
//    }
//    datasetIndex = 1;
//    specsModel = ModelFactory.createDefaultModel();
//    Resource inputDatasetUri = generateDatasetURI();
//    do {
//      Resource outputDatasetUri = generateDatasetURI();
//      // fix specs file for in/out datasets URIs
//      ResIterator moduleToChangeInput = specsModel
//        .listSubjectsWithProperty(DEER.hasInput, inputDatasetUri);
//      if (moduleToChangeInput.hasNext()) {
//        Resource r = moduleToChangeInput.next();
//        configBuilder.changeInputDatasetUri(specsModel, r, inputDatasetUri, outputDatasetUri);
////				specsModel.write(System.out,"TTL");
//      }
//      if (Math.random() >= complexity) {
//        // Create enrichment
//        IEnrichmentOperator module;
//        Map<String, String> parameters = null;
//        do {
//          module = getRandomModule();
//          parameters = generateRandomParameters(module, inputDataModel);
//        } while (parameters == null);
//        logger.info("Generating Module: " + module.getType() + " with parameters: " + parameters);
//        specsModel = configBuilder
//          .addModule(module, parameters, specsModel, inputDatasetUri, outputDatasetUri);
//        inputDatasetUri = getRandomDataset();
//      } else { // Create clone - merge sequence
//        List<Resource> outputDatasetstUris = addCloneOperator(inputDatasetUri);
//        addMergeOperator(outputDatasetstUris, outputDatasetUri);
////				specsModel.write(System.out,"TTL");
//        // in order not to create an empty clone merge sequence
//        inputDatasetUri = outputDatasetstUris.get(0);
//      }
//    } while (ConfigAnalyzer.getModules(specsModel).size() < size);
//    return specsModel;
//  }
//
//
//  /**
//   * @author sherif
//   */
//  private static Resource getRandomDataset() {
//    List<Resource> datasets = new ArrayList<Resource>(ConfigAnalyzer.getDatasets(specsModel));
//    if (datasets.size() == 0) {
//      return null;
//    }
//    int i = (int) (Math.random() * (datasets.size() - 1));
//    return datasets.get(i);
//  }
//
//  /**
//   * @author sherif
//   */
//  private static List<Resource> addCloneOperator(final Resource inputDatasetUri) {
//    IEnrichmentOperator clone = OperatorFactory.createOperator(OperatorFactory.CLONE_OPERATOR);
//    List<Model> confModels = new ArrayList<Model>(Arrays.asList(specsModel));
//    List<Resource> inputDatasets = new ArrayList<Resource>(Arrays.asList(inputDatasetUri));
//    List<Resource> outputDatasets = new ArrayList<Resource>(
//      Arrays.asList(generateDatasetURI(), generateDatasetURI()));
//    specsModel = configBuilder
//      .addOperator(clone, null, confModels, inputDatasets, outputDatasets);
//    return outputDatasets;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Resource addMergeOperator(final List<Resource> inputDatasetUris,
//    final Resource outputDatasetUri) {
//    List<Resource> outputDatasetsUris = new ArrayList<Resource>(Arrays.asList(outputDatasetUri));
//    IEnrichmentOperator merge = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
//    List<Model> confModels = new ArrayList<Model>(Arrays.asList(specsModel));
//    specsModel = configBuilder
//      .addOperator(merge, null, confModels, inputDatasetUris, outputDatasetsUris);
//    return outputDatasetUri;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> generateRandomParameters(final IEnrichmentOperator module,
//    final Model inputDataset) {
//    if (module instanceof DereferencingEnrichmentOperator) {
//      return DereferencingModuleRandomParameter(inputDataset);
//    } else if (module instanceof NLPEnrichmentOperator) {
//      return nlpModuleRandomParameter();
//    } else if (module instanceof AuthorityConformationEnrichmentOperator) {
//      return authorityConformationModuleRandomParameter(inputDataset);
//    } else if (module instanceof PredicateConformationEnrichmentOperator) {
//      return predicateConformationModuleRandomParameter(inputDataset);
//    } else if (module instanceof FilterEnrichmentOperator) {
//      return filterModuleRandomParameter(inputDataset);
//    } else if (module instanceof LinkingEnrichmentOperator) {
//      return linkingModuleRandomParameter(inputDataset);
//    }
//    return null;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> filterModuleRandomParameter(final Model inputDataset) {
//    Map<String, String> parameters = new HashMap<String, String>();
//    int l = (int) (1 + Math.random() * 5);
//    List<String> predicates = getPredicates(inputDataset, l);
//    if (predicates.size() == 0) {
//      return null;
//    }
//    String triplePattern = "";
//    for (String p : predicates) {
//      triplePattern += p + " ";
//    }
//    parameters.put(FilterEnrichmentOperator.TRIPLES_PATTERN, triplePattern);
//    return parameters;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> predicateConformationModuleRandomParameter(
//    final Model inputDataset) {
//    Map<String, String> parameters = new HashMap<String, String>();
//    int l = (int) (1 + Math.random() * 5);
//    List<String> predicates = getPredicates(inputDataset, l);
//    if (predicates.size() == 0) {
//      return null;
//    }
//    int i = 1;
//    for (String p : predicates) {
//      parameters.put(PredicateConformationEnrichmentOperator.SOURCE_PROPERTY + i++, p);
//      parameters.put(PredicateConformationEnrichmentOperator.TARGET_PROPERTY + i++, p + i);
//    }
//    return parameters;
//  }
//
//  /**
//   * @param limit number of returned predicates, all predicates returned if set to -1
//   * @return List of predicates in inputDataset
//   * @author sherif
//   */
//  public static List<String> getPredicates(final Model inputDataset, int limit) {
//    String sparqlQueryString = "SELECT DISTINCT ?p {?s ?p ?o} " +
//      ((limit == -1) ? "" : "LIMIT " + limit);
//    QueryFactory.create(sparqlQueryString);
//    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, inputDataset);
//    ResultSet queryResults = qexec.execSelect();
//    List<String> predicates = new ArrayList<String>();
//    while (queryResults.hasNext()) {
//      QuerySolution qs = queryResults.nextSolution();
//      String p = qs.getResource("?p").toString();
//      predicates.add(p);
//    }
//    qexec.close();
//    return predicates;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> authorityConformationModuleRandomParameter(
//    final Model inputDataset) {
//    Map<String, String> parameters = new HashMap<String, String>();
//    String authority = "";
//    long offset = (long) (Math.random() * (inputDataset.size() - 1));
//    String sparqlQueryString = "SELECT ?s {?s ?p ?o} LIMIT 1 OFFSET " + offset;
//    QueryFactory.create(sparqlQueryString);
//    QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, inputDataset);
//    ResultSet queryResults = qexec.execSelect();
//    if (queryResults.hasNext()) {
//      QuerySolution qs = queryResults.nextSolution();
//      authority = qs.getResource("?s").toString();
//    } else {
//      qexec.close();
//      return null;
//    }
//    qexec.close();
//    if (authority.contains("#")) {
//      authority = authority.substring(0, authority.lastIndexOf("#"));
//    } else {
//      authority = authority.substring(0, authority.lastIndexOf("/"));
//    }
////    parameters.put(AuthorityConformationEnrichmentOperator.SOURCE_SUBJECT_AUTHORITY, authority);
////    parameters
////      .put(AuthorityConformationEnrichmentOperator.TARGET_SUBJECT_AUTHORITY, "http://example.com/resource/");
//    return parameters;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> linkingModuleRandomParameter(final Model inputDataset) {
//    return null;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> nlpModuleRandomParameter() {
//    Map<String, String> parameters = new HashMap<String, String>();
//    double r = Math.random();
//    if (r > 0.75) {
//      parameters.put(NLPEnrichmentOperator.NER_TYPE, NLPEnrichmentOperator.LOCATION);
//    } else if (r > 0.5) {
//      parameters.put(NLPEnrichmentOperator.NER_TYPE, NLPEnrichmentOperator.PERSON);
//    } else if (r > 0.25) {
//      parameters.put(NLPEnrichmentOperator.NER_TYPE, NLPEnrichmentOperator.ORGANIZATION);
//    } else {
//      parameters.put(NLPEnrichmentOperator.NER_TYPE, NLPEnrichmentOperator.ALL);
//    }
//    return parameters;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static Map<String, String> DereferencingModuleRandomParameter(final Model inputDataset) {
//    Map<String, String> parameters = new HashMap<String, String>();
//    int l = (int) (1 + Math.random() * 5);
//    List<String> predicates = getPredicates(inputDataset, l);
//    int i = 1;
//    for (String p : predicates) {
//      parameters.put(DereferencingEnrichmentOperator.INPUT_PROPERTY + i++, p);
//      parameters.put(DereferencingEnrichmentOperator.OUTPUT_PROPERTY + i++, p + i);
//    }
//    return parameters;
//  }
//
//  /**
//   * @author sherif
//   */
//  private static IEnrichmentOperator getRandomModule() {
//    int i = (int) (Math.random() * deerModules.size());
//    return deerModules.get(i);
//  }
//
//  @SuppressWarnings("unused")
//  private static Object getRandomModuleOrOperator(Model inputDataset) {
//    if (Math.random() > 0.5) {
//      return getRandomModule();
//    } else {
//      int i = (int) (Math.random() * deerOperators.size());
//      return deerOperators.get(i);
//    }
//  }
//
//  private static Resource generateDatasetURI() {
//    return ResourceFactory.createResource(DEER.uri + "dataset_" + datasetIndex++);
//  }
//
//
//  /**
//   * @author sherif
//   */
//  public static void main(String[] args) {
////		Model kb = new Reader().readModel(args[0]);
////		Model m = g.generateSpecs(kb, 5, 0.5);
//    Model m = RandomSpecsGenerator.generateSpecs(args[0], 2, .2);
//    m.write(System.out, "TTL");
//    System.out.println("Module count: " + ConfigAnalyzer.getModules(m).size());
//    System.out.println("Operators count: " + ConfigAnalyzer.getOperators(m).size());
//
//  }

}
