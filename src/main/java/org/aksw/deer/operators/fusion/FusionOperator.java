/**
 * 
 */
package org.aksw.deer.operators.fusion;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.deer.io.Reader;
import org.aksw.deer.operators.DeerOperator;
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
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class FusionOperator implements DeerOperator {
	private static final Logger logger = Logger.getLogger(FusionOperator.class.getName());
	private static final String DBPEDIA = "http://dbpedia.org/sparql";
	private static final String DBPEDIA_SAKE = "http://sake.informatik.uni-leipzig.de:8890/sparql";
	private static final String DBPEDIA_ONTOLOGY_201504 = "src/main/resources/fusion/dbpedia_201504.owl";
	private static final String DBPEDIA_ONTOLOGY_3_9 	= "src/main/resources/fusion/dbpedia_3.9.owl";
	private static final String DBPEDIA_ONTOLOGY 	= "src/main/resources/fusion/dbpedia.owl";
	public static final String FUNCTIONAL_PROPERTY ="functionalproperty";
	private static final String POSITIVE_EXAMPLE = "positiveexample";
	private static final String KB_NAMES = "kbnames";

	public static Map<String, OWLClassExpression> langTag2ClsExp = new HashMap<>();

	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(final List<Model> models,final Map<String, String> parameters) {
		List<Model> result = new ArrayList<Model>();
		logger.info("--------------- Fusion Operator ---------------");
		Set<Property> funcProp = new HashSet<>();

		//		Read parameters
		Model posExMdl = ModelFactory.createDefaultModel();
		List<String> kbNames = new ArrayList<>();
		if(parameters != null ){
			for(String key : parameters.keySet()){
				if(key.toLowerCase().startsWith(FUNCTIONAL_PROPERTY)){
					funcProp.add(ResourceFactory.createProperty(parameters.get(key)));
				}else if(key.toLowerCase().startsWith(POSITIVE_EXAMPLE)){
					posExMdl = Reader.readModel((parameters.get(key)));
				}else if(key.toLowerCase().startsWith(KB_NAMES)){
					kbNames.addAll(Arrays.asList(parameters.get(key).split(" "))) ;
				}
			}
		}
		if(posExMdl.size() == 0){
			logger.error("No positive examples found, exit with error");
			System.exit(1);
		}

		//		For each knowledge base, collect the triples which were selected from it
		List<Model> posTriplesMdls = new ArrayList<>();
		for(Model m : models){
			posTriplesMdls.add(posExMdl.intersection(m));
		}

		//		for each property:
		//			perform a learning task per input knowledge base:
		//			option 1: use the subjects of selected triples as input
		//			option 2: introduce another resource r per triple:
		//			r hasSubject s
		//			r hasPredicate p
		//			r hasObject o
		//			r [attach all features specific to r, e.g., length of Wikipedia page]
		//			r [attach all features specific for this (s,p) combination here]
		//		generate negative examples: take other resources which use the same property
		//		learn a set of class expressions as classifier (also store the confidence value)

		for (Property fp : funcProp) { 
			for(int i = 0 ;  i < posTriplesMdls.size() ; i++){
				logger.debug("setting up positive and negative examples...");
				// Positive examples
				Set<OWLIndividual> pos = new HashSet<>();
				Model posEx = ModelFactory.createDefaultModel();
				StmtIterator listStatements = posTriplesMdls.get(i).listStatements(null, fp, (RDFNode) null);
				while(listStatements.hasNext()){
					Statement stat = listStatements.next();
					pos.add(new OWLNamedIndividualImpl(IRI.create(stat.getSubject().getURI().toString())));
					posEx.add(stat);

				}
				// Negative examples
				Set<OWLIndividual> neg = new HashSet<>();
				Model negEx = ModelFactory.createDefaultModel();
				listStatements = models.get(i).listStatements(null, fp, (RDFNode) null);
				while(listStatements.hasNext()){
					Statement stat = listStatements.next();
					if(!posEx.contains(stat)){
						neg.add(new OWLNamedIndividualImpl(IRI.create(stat.getSubject().getURI().toString())));
						//						negEx.add(stat);
					}
				}
				logger.debug("finished setting up examples");

				// Use DL-Learner to learn class expressions
				try {
					learnOWLClassExpression(pos, neg, DBPEDIA_SAKE);
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}


	public static OWLClassExpression learnOWLClassExpression(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples,String endPoint) throws ComponentInitException{
		logger.info("starting learning task ...");
		logger.info("initializing knowledge source...");
		Model model = ModelFactory.createDefaultModel();
		OWLOntology ontology = getOWLOntology(Reader.readModel(DBPEDIA_ONTOLOGY));
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		logger.info("finished initializing knowledge source");

		logger.info("initializing reasoner...");
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		//		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		SparqlKnowledgeSource fragmentExtractor = new SparqlKnowledgeSource();
		fragmentExtractor.setUseImprovedSparqlTupelAquisitor(false);
		try {
			fragmentExtractor.setUrl(new URL(endPoint));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		fragmentExtractor.setInstances(getInstanceAsStrings(posExamples, negExamples));
		System.err.println(getInstanceAsStrings(posExamples, negExamples));
		fragmentExtractor.setRecursionDepth(3);
		fragmentExtractor.setPredefinedFilter("YAGO");
		fragmentExtractor.setDefaultGraphURIs(Sets.newHashSet("http://dbpedia.org"));
		//		fragmentExtractor.setGetAllSuperClasses(true);
		fragmentExtractor.init();
		// rc for reasoner component
		ClosedWorldReasoner rc = new ClosedWorldReasoner();
		rc.setSources(fragmentExtractor);
		//		rc.setSources(ks, fragmentExtractor);
		//baseReasoner.init();
		//rc.setReasonerComponent(baseReasoner);
		rc.init();
		logger.info("finished initializing reasoner");

		logger.info("initializing learning problem...");
		// lp for learning problem
		AbstractClassExpressionLearningProblem lp;
		if(negExamples.isEmpty()){
			lp = new PosOnlyLP(rc);
			((PosOnlyLP) lp).setPositiveExamples(new TreeSet(posExamples));
		}else{
			lp = new PosNegLPStandard(rc);
			((PosNegLP) lp).setPositiveExamples(posExamples);
			((PosNegLP) lp).setNegativeExamples(negExamples);
		}
		lp.init();
		logger.info("finished initializing learning problem");

		logger.info("initializing learning algorithm...");
		// la for learning algorithm
		CELOE la = new CELOE(lp, rc);
		OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
		heuristic.setExpansionPenaltyFactor(0.1);
		heuristic.init();
		la.setHeuristic(heuristic);
		la.setMaxExecutionTimeInSeconds(3);
		la.setNoisePercentage(80);
		la.setMaxNrOfResults(50);
		la.setExpandAccuracy100Nodes(true);
		// start class can also be omitted -- will be owl:Thing then
		//        OWLClassExpression startClass = new OWLClassImpl(
		//        		IRI.create("http://dl-learner.org/smallis/Allelic_info"));
		//        la.setStartClass(startClass);
		la.init();
		logger.info("finished initializing learning algorithm");

		// runs the actual learning task and usually prints the results to stdout
		la.start();
		return ((CELOE) la).getCurrentlyBestDescriptions(1).get(0);

		// stuff I typed for demonstration when you where in 635:
		//        List<OWLClassExpression> foo = ((CELOE) la).getCurrentlyBestDescriptions(10);
		//		        OWLClassImpl bar = new OWLClassImpl(IRI.create("http://ex.org/sth"));
		//        la.getLearningProblem().getAccuracy(bar);
		//		        boolean res = rc.hasType(bar, new OWLNamedIndividualImpl(IRI.create("http://foo.bar/indiv")));
	}


	public static OWLOntology getOWLOntology(final Model model) {
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



	@SafeVarargs
	private static Set<String> getInstanceAsStrings(Set<OWLIndividual> ... individualSets) {
		Set<String> result = new HashSet<>();
		for(Set<OWLIndividual> individualSet : individualSets){
			for(OWLIndividual i : individualSet){
				result.add(i.toString().replace("<", "").replace(">", ""));
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		return parameters;
	}

	public static void main(String args[]) throws IOException, ComponentInitException{
		//Logger.getLogger("org.dllearner").setLevel(Level.TRACE);
		//		test4En(args[0]);
//		test4AllLangTags(args[0]);
		getBestOfAllLangTags(args[0], args[0]);
	}



	public static void test4En(String inputFile) throws IOException, ComponentInitException{
		Examples examples = new Examples(inputFile);
		int exCnt = 10;
		Set<OWLIndividual> positiveExamples = examples.getPositiveExamples("en",exCnt);
		Set<OWLIndividual> negativeExamples = examples.getNegativeExamples("en",exCnt);
		LearningTask lt = new LearningTask(positiveExamples, negativeExamples, "en");
		lt.getBestDescription();
		System.out.println("------------------ POS EX ------------------");
		for(OWLIndividual oi : positiveExamples){
			System.out.println(oi + " ---> " +lt.isValidIndividual(oi));
		}
		System.out.println("------------------ NEG EX ------------------");
		for(OWLIndividual oi : negativeExamples){
			System.out.println(oi + " ---> " +lt.isValidIndividual(oi));
		}
	}

	public static Map<OWLIndividual, Map<String, Double>>  getBestOfAllLangTags(String trainingFile, String testingFile) throws IOException, ComponentInitException{
		int exCnt = 10;
		Map<OWLIndividual, Map<String, Double>> testEx2Langtag2Score = new HashMap<>(); 
		String langTags[] = {"en", "de", "es"};
		Examples examples = new Examples(trainingFile);
		Examples testExamples = new Examples(testingFile);
		
		
		for(String langTag : langTags){
			System.err.println("********** Working for " + langTag);
			
			Set<OWLIndividual> positiveExamples = examples.getPositiveExamples(langTag,exCnt);
			Set<OWLIndividual> negativeExamples = examples.getNegativeExamples(langTag,exCnt);
			Set<OWLIndividual> posTestExamples = testExamples.getPositiveExamples(langTag,exCnt);
			
			LearningTask lt = new LearningTask(positiveExamples, negativeExamples, langTag);
			EvaluatedDescription<? extends Score> bestDesc = lt.getBestDescription();

			double bestAccuracy = bestDesc.getAccuracy();
//			OWLClassExpression BestClsExp = bestDesc.getDescription();
			for(OWLIndividual oi : posTestExamples){ 
				if(lt.isValidIndividual(oi)){
					if(!testEx2Langtag2Score.containsKey(oi)){
						Map<String, Double> langTag2Score = new HashMap<>();
						langTag2Score.put(langTag, bestAccuracy);
						testEx2Langtag2Score.put(oi, langTag2Score);
					}else{
						double oldAccuracy = testEx2Langtag2Score.get(oi).values().iterator().next();
						if(bestAccuracy > oldAccuracy){
							Map<String, Double> langTag2Score = new HashMap<>();
							langTag2Score.put(langTag, bestAccuracy);
							testEx2Langtag2Score.put(oi, langTag2Score);
						} 
					}

				}
			}
		}
		
		// Result output
		System.out.println("Results:\n" + testEx2Langtag2Score);
		Set<OWLIndividual>
			enRefInd = testExamples.getPositiveExamples("en", exCnt),
			deRefInd = testExamples.getPositiveExamples("de", exCnt),
			esRefInd = testExamples.getPositiveExamples("ed", exCnt),
			allRefInd = new HashSet<>(),
			enInd = new HashSet<>(),
			deInd = new HashSet<>(),
			esInd = new HashSet<>(),
			allInd = new HashSet<>();
		for(OWLIndividual ind :testEx2Langtag2Score.keySet()){
			switch(testEx2Langtag2Score.get(ind).keySet().iterator().next()){
				case "en":
					enInd.add(ind);
				case "de":
					deInd.add(ind);
				case "es":
					esInd.add(ind);
			}
		}
		System.out.println("en\t" + PRFComputer.computeFScore(enInd, enRefInd));
		System.out.println("de\t" + PRFComputer.computeFScore(deInd, deRefInd));
		System.out.println("es\t" + PRFComputer.computeFScore(esInd, esRefInd));
		
		allRefInd.addAll(enRefInd);
		allRefInd.addAll(deRefInd);
		allRefInd.addAll(esRefInd);
		allInd.addAll(enInd);
		allInd.addAll(deInd);
		allInd.addAll(esInd);
		System.out.println("Overall\t" + PRFComputer.computeFScore(allInd, allRefInd));
		return testEx2Langtag2Score;
	}

	public static void test4AllLangTags(String inputFile) throws IOException, ComponentInitException{
		String resutStr = new String();
		Multimap<String, Map<String, Boolean>> posEx2Langtag2tScore = ArrayListMultimap.create(); 
		Multimap<String, Map<String, Boolean>> negEx2Langtag2tScore = ArrayListMultimap.create(); 
		String langTags[] = {"en", "de", "es"};
		Examples examples = new Examples(inputFile);
		for(String langTag : langTags){
			System.err.println("********** Working for " + langTag);
			int exCnt = 10;
			Set<OWLIndividual> positiveExamples = examples.getPositiveExamples(langTag,exCnt);
			Set<OWLIndividual> negativeExamples = examples.getNegativeExamples(langTag,exCnt);
			LearningTask lt = new LearningTask(positiveExamples, negativeExamples, langTag);
			lt.getBestDescription();

			for(OWLIndividual oi : positiveExamples){
				boolean isValid = lt.isValidIndividual(oi);
				Map<String, Boolean> langtag2tScore = new HashMap<>();
				langtag2tScore.put(langTag, isValid);
				posEx2Langtag2tScore.put(oi.toString(), langtag2tScore);
			}

			for(OWLIndividual oi : negativeExamples){
				boolean isValid = lt.isValidIndividual(oi);
				Map<String, Boolean> langtag2tScore = new HashMap<>();
				langtag2tScore.put(langTag, isValid);
				negEx2Langtag2tScore.put(oi.toString(), langtag2tScore);
			}
		}
		System.out.println("------------------ POS EX ------------------");
		System.out.println(posEx2Langtag2tScore);
		System.out.println("------------------ NEG EX ------------------");
		System.out.println(negEx2Langtag2tScore);
	}

	public static Map<String, OWLClassExpression> _learnFromExamples(String inputFile) throws IOException, ComponentInitException{
		Examples examples = new Examples(inputFile);
		for(String langTag : examples.getAvailableLanguageTags()){
			if(getEndPoint(langTag) != null){
				int ExampleCount = 10;
				OWLClassExpression bestExpr = learnOWLClassExpression(examples.getPositiveExamples("en",ExampleCount), examples.getNegativeExamples("en",ExampleCount), getEndPoint(langTag));
				langTag2ClsExp.put(langTag,bestExpr);
			}
		}
		return langTag2ClsExp;
	}

	public static String getBestLanguageEdition(){
		for ( String langTag : langTag2ClsExp.keySet()) {
			if(true){
				//TODO to be completed 
			}
		}
		//		OWLClassExpression expr : langTag2ClsExp.values()

		// stuff I typed for demonstration when you where in 635:
		//        List<OWLClassExpression> foo = ((CELOE) la).getCurrentlyBestDescriptions(10);
		//        OWLClassImpl bar = new OWLClassImpl(IRI.create("http://ex.org/sth"));
		//        la.getLearningProblem().getAccuracy(bar);
		//        boolean res = rc.hasType(bar, new OWLNamedIndividualImpl(IRI.create("http://foo.bar/indiv")));
		return null;

	}

	public static String getEndPoint(String langTag){
		Map<String, String> langTag2Endpoint = new HashMap<>();
		langTag2Endpoint.put("en", DBPEDIA_SAKE);
		langTag2Endpoint.put("es", "http://es.dbpedia.org/sparql");
		langTag2Endpoint.put("eu", "http://eu.dbpedia.org/sparql");
		//		langTag2Endpoint.put("it", "http://it.dbpedia.org/sparql");
		return langTag2Endpoint.get(langTag);

	}

	public static void test(){
		URL path = FusionOperator.class.getClassLoader().getResource("fusion/");
		List<Model> testMdls = new ArrayList<>(); 
		for(int i = 1 ; i<= 3 ; i++){
			testMdls.add(Reader.readModel(path + "s" + i +".nt"));
		}
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(FUNCTIONAL_PROPERTY, "http://dbpedia.org/ontology/birthDate");
		parameters.put(POSITIVE_EXAMPLE, path + "pos.nt");
		parameters.put(KB_NAMES, "s1 s2 s3");
		FusionOperator f = new FusionOperator();
		f.process(testMdls, parameters);
	}
}
