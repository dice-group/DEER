/**
 *
 */
package org.aksw.deer.plugin.operator.fusion;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.aksw.deer.io.ModelReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.Score;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import com.clarkparsia.sparqlowl.parser.antlr.SparqlOwlParser.defaultGraphClause_return;

/**
 * @author sherif
 */
public class LearningTask {

  private static final Logger logger = Logger.getLogger(LearningTask.class.getName());
  private static final String DBPEDIA_SAKE = "http://sake.informatik.uni-leipzig.de:8890/sparql";
  public static String PRE_DEFINED_FILTER = "YAGO";
  public String graph = new String();
  public String ontologyURI = "src/main/resources/org.aksw.deer.resources.fusion/dbpedia_201504.owl";
  protected String langTag = new String();
  protected String endPoint = new String();
  protected OWLClassExpression currentlyBestDescription = null;
  protected Set<OWLIndividual> posExamples = new HashSet<>();
  protected Set<OWLIndividual> negExamples = new HashSet<>();


  ClosedWorldReasoner reasoner = new ClosedWorldReasoner();
  SparqlKnowledgeSource fragmentExtractor = new SparqlKnowledgeSource();
  AbstractClassExpressionLearningProblem learningProblem;
  CELOE learningAlgorithm;


  LearningTask(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples, String langTag) {
    this.posExamples = posExamples;
    this.negExamples = negExamples;
    this.langTag = langTag;
    switch (this.langTag) {
      case "en":
        this.endPoint = DBPEDIA_SAKE;
        this.graph = "http://dbpedia.org";
        break;
      case "de":
        this.endPoint = "http://de.dbpedia.org/sparql";
        this.graph = "http://de.dbpedia.org";
        break;
      case "es":
        this.endPoint = "http://es.dbpedia.org/sparql";
        this.graph = "http://es.dbpedia.org";
        break;
      case "eu":
        this.endPoint = "http://eu.dbpedia.org/sparql";
        this.graph = "http://eu.dbpedia.org";
        break;
      case "it":
        this.endPoint = "http://it.dbpedia.org/sparql";
        this.graph = "http://it.dbpedia.org";
        break;
      default:
        logger.error(langTag + "is undefined");
        System.exit(1);
    }
  }

  public boolean isValidIndividual(OWLIndividual owlIndividual) {
    return reasoner.hasType(currentlyBestDescription, owlIndividual);
  }


  public EvaluatedDescription<? extends Score> getBestDescription() throws ComponentInitException {
    logger.info("starting learning task ...");
    logger.info("initializing knowledge source...");
    Model model = ModelFactory.createDefaultModel();
    OWLOntology ontology = getOWLOntology((new ModelReader()).readModel(ontologyURI));
    KnowledgeSource ks = new OWLAPIOntology(ontology);
    ks.init();
    logger.info("finished initializing knowledge source");

    logger.info("initializing reasoner...");
    OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
    //		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
    fragmentExtractor = new SparqlKnowledgeSource();
    fragmentExtractor.setUseImprovedSparqlTupelAquisitor(false);
    try {
      fragmentExtractor.setUrl(new URL(endPoint));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    fragmentExtractor.setInstances(getInstanceAsStrings(posExamples, negExamples));
    System.err.println(getInstanceAsStrings(posExamples, negExamples));
    fragmentExtractor.setRecursionDepth(1);
    fragmentExtractor.setPredefinedFilter(PRE_DEFINED_FILTER);
    fragmentExtractor.setDefaultGraphURIs(Sets.newHashSet(graph));
    //		fragmentExtractor.setGetAllSuperClasses(true);
    fragmentExtractor.init();

    reasoner = new ClosedWorldReasoner();
    reasoner.setSources(fragmentExtractor);
    reasoner.init();
    logger.info("finished initializing reasoner");

    logger.info("initializing learning problem...");
    if (negExamples.isEmpty()) {
      learningProblem = new PosOnlyLP(reasoner);
      ((PosOnlyLP) learningProblem).setPositiveExamples(new TreeSet(posExamples));
    } else {
      learningProblem = new PosNegLPStandard(reasoner);
      ((PosNegLP) learningProblem).setPositiveExamples(posExamples);
      ((PosNegLP) learningProblem).setNegativeExamples(negExamples);
    }
    System.err.println(endPoint);
    learningProblem.init();
    logger.info("finished initializing learning problem");

    logger.info("initializing learning algorithm...");
    // la for learning algorithm
    learningAlgorithm = new CELOE(learningProblem, reasoner);
    OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
    heuristic.setExpansionPenaltyFactor(0.1);
    heuristic.init();
    learningAlgorithm.setHeuristic(heuristic);
    learningAlgorithm.setMaxExecutionTimeInSeconds(3);
    learningAlgorithm.setNoisePercentage(80);
    learningAlgorithm.setMaxNrOfResults(50);
    learningAlgorithm.setExpandAccuracy100Nodes(true);
    learningAlgorithm.init();
    logger.info("finished initializing learning algorithm");

    // runs the actual learning task and usually prints the results to stdout
    learningAlgorithm.start();

    EvaluatedDescription<? extends Score> desc = learningAlgorithm
      .getCurrentlyBestEvaluatedDescription();
    //		double accuricy = desc.getAccuracy();
    currentlyBestDescription = desc.getDescription();
    return desc;
  }


  public OWLOntology getOWLOntology(final Model model) {
    OWLOntology ontology;
    try (PipedInputStream is = new PipedInputStream();
      PipedOutputStream os = new PipedOutputStream(is);) {
      OWLOntologyManager man = OWLManager.createOWLOntologyManager();
      new Thread(new Runnable() {
        @Override
        public void run() {
          model.write(os, "TURTLE", null);
          try {
            os.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }).start();
      ontology = man.loadOntologyFromOntologyDocument(is);
      return ontology;
    } catch (Exception e) {
      throw new RuntimeException("Could not convert JENA API model to OWL API ontology.", e);
    }
  }

  @SuppressWarnings("unchecked")
  private Set<String> getInstanceAsStrings(Set<OWLIndividual>... individualSets) {
    Set<String> result = new HashSet<>();
    for (Set<OWLIndividual> individualSet : individualSets) {
      for (OWLIndividual i : individualSet) {
        result.add(i.toString().replace("<", "").replace(">", ""));
      }
    }
    return result;
  }

}
