/**
 * 
 */
package org.aksw.deer.operators.fusion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.deer.io.Reader;
import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.operators.DeerOperator;
import org.apache.jena.riot.Lang;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
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
	public static final String FUNCTIONAL_PROPERTY ="functionalproperty";
	private static final String POSITIVE_EXAMPLE = "positiveexample";
	private static final String KB_NAMES = "kbnames";

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
					learn(models.get(i), pos, neg);
				} catch (ComponentInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}


	public static void learn(Model kbMdl, Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples) throws ComponentInitException{
		logger.debug("starting learning task ...");
		logger.debug("initializing knowledge source...");
		/*
		 * Reads a Jena model and later converts it to an OWLAPI ontology
		 * object. Just for demonstration, of course. You could also load a
		 * file into an OWLAPI ontology directly.
		 */
		Model model = ModelFactory.createDefaultModel();

		//		try (InputStream is = new FileInputStream(new File(kbFilePath))) {
		//			model.read(is, null, Lang.RDFXML.getName());
		//		}
		OWLOntology ontology = getOWLOntology(model);
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		logger.debug("finished initializing knowledge source");

		logger.debug("initializing reasoner...");
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		baseReasoner.init();

		// rc for reasoner component
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.setReasonerComponent(baseReasoner);
		rc.init();
		logger.debug("finished initializing reasoner");


		//		logger.debug("setting up positive and negative examples...");
		//		Set<OWLIndividual> posExamples = Sets.newHashSet(
		//				new OWLNamedIndividualImpl(IRI.create("http://ex.org/posInidiv01")),
		//				new OWLNamedIndividualImpl(IRI.create("http://ex.org/posInidiv02")));
		//
		//		Set<OWLIndividual> negExamples = Sets.newHashSet(
		//				new OWLNamedIndividualImpl(IRI.create("http://ex.org/negInidiv01")),
		//				new OWLNamedIndividualImpl(IRI.create("http://ex.org/negInidiv02")));
		//		logger.debug("finished setting up examples");


		logger.debug("initializing learning problem...");
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
		logger.debug("finished initializing learning problem");

		logger.debug("initializing learning algorithm...");
		// la for learning algorithm
		CELOE la = new CELOE(lp, rc);

		OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
		heuristic.setExpansionPenaltyFactor(0.1);
		heuristic.init();
		la.setHeuristic(heuristic);
		la.setMaxExecutionTimeInSeconds(1800);
		la.setNoisePercentage(80);
		la.setMaxNrOfResults(50);
		la.setExpandAccuracy100Nodes(true);
		// start class can also be omitted -- will be owl:Thing then
		//        OWLClassExpression startClass = new OWLClassImpl(
		//        		IRI.create("http://dl-learner.org/smallis/Allelic_info"));
		//        la.setStartClass(startClass);
		la.init();
		logger.debug("finished initializing learning algorithm");

		// runs the actual learning task and usually prints the results to stdout
		la.start();

		// stuff I typed for demonstration when you where in 635:
		//        List<OWLClassExpression> foo = ((CELOE) la).getCurrentlyBestDescriptions(10);
		//        OWLClassImpl bar = new OWLClassImpl(IRI.create("http://ex.org/sth"));
		//        la.getLearningProblem().getAccuracy(bar);
		//        boolean res = rc.hasType(bar, new OWLNamedIndividualImpl(IRI.create("http://foo.bar/indiv")));
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
			throw new RuntimeException(
					"Could not convert JENA API model to OWL API ontology.", e);
		}
	}




	/**
	 * @param pModel
	 * @return
	 * @author sherif
	 */
	private Model addAdditionalTriples(Model pModel) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @param m
	 * @param fp
	 * @return
	 * @author sherif
	 */
	private Model getAssociatedTriples(Model m, Property fp) {
		Model result = ModelFactory.createDefaultModel();
		StmtIterator listStatements = m.listStatements(null, fp, (RDFNode) null);
		while(listStatements.hasNext()) {
			Statement s = listStatements.next();
			result.createReifiedStatement(s);
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

	public static void main(String args[]){
		test();
	}

	public static void test(){
		String path = "/home/sherif/JavaProjects/GeoKnow/DEER/src/main/resources/fusion/";
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
