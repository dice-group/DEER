//package org.aksw.deer.learning.genetic;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import org.aksw.deer.learning.FitnessFunction;
//import org.aksw.deer.learning.RandomUtil;
//import org.apache.commons.io.FileUtils;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.StringReader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.IntStream;
//
///**
// *
// */
//public class HPOPhase3 {
//
//  private static TrainingData trainingData;
//  private static String testDir = "./test/";
//  private static String[] paths = new String[]{
//    "a.ttl",
//    "b.ttl",
//    "c.ttl",
//    "d.ttl"
//  };
//
//  @BeforeClass
//  public static void setUp() throws IOException {
//    if (!Files.exists(Paths.get(testDir))) {
//      Files.createDirectory(Paths.get(testDir));
//    }
//
//    Model a = ModelFactory.createDefaultModel().read(new StringReader(
//      "@prefix ex: <http://example.org/>." +
//        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
//        "ex:table ex:madeOf ex:wood ." +
//        "ex:chair ex:nextTo ex:table ." +
//        "ex:table rdfs:comment \"This table has been manufactured in Leipzig.\" ." +
//        "ex:chair rdfs:comment \"This chair has been manufactured in Leipzig.\" ." +
//      ""), null, "TTL");
//
//    Model b = ModelFactory.createDefaultModel().read(new StringReader(
//      "@prefix ex2: <http://example2.org/>." +
//        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
//        "ex2:university rdfs:label \"University of Leipzig\"." +
//        "ex2:university rdfs:comment \"The University of Leipzig has been founded in 1409.\" ." +
//        ""), null, "TTL");
//// (a -> )(b -> AC ->) -> MERGE -> NER -> PC -> DEREF
//    Model target = ModelFactory.createDefaultModel().read(new StringReader(
//      "@prefix ex: <http://example.org/>." +
//        "@prefix foxo:  <http://ns.aksw.org/fox/ontology#> ." +
//        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
//        "ex:university rdfs:label \"University of Leipzig\"." +
//        "ex:university foxo:relatedTo <http://dbpedia.org/resource/Leipzig> ." +
//        "ex:university rdfs:comment \"The University of Leipzig has been founded in 1409.\" ." +
//        "ex:table ex:madeOf ex:wood ." +
//        "ex:chair ex:inFrontOf ex:table ." +
//        "ex:table rdfs:comment \"This table has been manufactured in Leipzig.\" ." +
//        "ex:table foxo:relatedTo <http://dbpedia.org/resource/Leipzig> ." +
//        "ex:chair rdfs:comment \"This chair has been manufactured in Leipzig.\" ." +
//      "ex:chair foxo:relatedTo <http://dbpedia.org/resource/Leipzig> ." +
//      "<http://dbpedia.org/resource/Leipzig> <http://dbpedia.org/ontology/country> <http://dbpedia.org/resource/Germany> ."
//    ), null, "TTL");
//    a.write(new FileWriter(testDir + paths[0]), "TTL");
//    b.write(new FileWriter(testDir + paths[1]), "TTL");
//    target.write(new FileWriter(testDir + paths[2]), "TTL");
//    trainingData = new TrainingData(new FitnessFunction(new int[]{1,1,1,1}, 1), List.of(testDir + paths[0], testDir + paths[1]), List.of(testDir + paths[0], testDir + paths[1]), testDir + paths[2], testDir + paths[2], testDir + paths[3]);
//    Genotype.SIZE = 8;
//  }
//
//  @AfterClass
//  public static void tearDown() throws IOException {
//    if (Files.exists(Paths.get(testDir))) {
//      FileUtils.deleteDirectory(Paths.get(testDir).toFile());
//    }
//  }
//
//  @Test
//  public void simpleTest() {
//    List<PopulationEvaluationResult> evaluationResults = getAlg(0.0, 0.3, 0.2).run();
//    System.out.println(evaluationResults.size());
//    System.out.println(Phenotype.of(evaluationResults.get(evaluationResults.size()-1).getBest()));
//    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().compactBestResult(false, 0));
//    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().getBestFitness());
//  }
//
//  @Test
//  public void withoutSemantic() {
//    runSimpleExperiment(0.0, 0.1, 0.1);
//  }
//
//  @Test
//  public void constructorTest() throws InterruptedException {
//
//    runSimpleExperiment(0.6,0.5,0.5);
////    0.60	0.50	0.50	31.36	37.86	3.00	266.00	1.00	200
//    //0.60	0.50	0.50	41.35	50.48	4.00	225.00	1.00	20
//
////    List<Thread> threads = new ArrayList<>();
////    for (double oF = 0; oF <= 1; oF+=.2) {
////      for (double mP = 0.1; mP <= 0.5; mP+=.2) {
////        for (double mR = 0.1; mR <= 0.5; mR+=.2) {
////          final double a = oF;
////          final double b = mP;
////          final double c = mR;
////          threads.add(Executors.defaultThreadFactory().newThread(() -> runSimpleExperiment(a, b, c)));
////        }
////      }
////    }
////    int k = 0;
////    while (k < threads.size()) {
////      for (int i = 0; i < 8; i++) {
////        if (k+i < threads.size()) {
////          threads.get(k+i).start();
////        }
////      }
////      for (int i = 0; i < 8; i++) {
////        if (k+i < threads.size()) {
////          threads.get(k+i).join();
////        }
////      }
////      k+=8;
////    }
//
//  }
//
//  private void runSimpleExperiment(double oF, double mP, double mR) {
//    int it = 200;
//    AtomicInteger i = new AtomicInteger(0);
//    AtomicDouble d = new AtomicDouble(0);
//    PopulationEvaluationResult.DoubleStatistics statistics = IntStream.range(0, it)
//      .mapToDouble(j -> {
//        GeneticProgrammingAlgorithm alg = getAlg(oF, mP, mR);
//        List<PopulationEvaluationResult> evaluationResults = alg.run();
//        double max = evaluationResults.get(evaluationResults.size() - 1).getMax();
//        d.addAndGet(max);
//        if (max == 1.0) {
//          i.incrementAndGet();
//        }
//        return evaluationResults.size();
//      })
//      .collect(PopulationEvaluationResult.DoubleStatistics::new,
//        PopulationEvaluationResult.DoubleStatistics::accept,
//        PopulationEvaluationResult.DoubleStatistics::combine);
//    System.out.println(String.format(Locale.ENGLISH, "%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", oF, mP, mR, statistics.getAverage(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax(), d.get()/it, i.get()));
////    System.out.println(oF  + "\t" +  mP  + "\t" +  mR + "\t" + statistics.getAverage() + "\t" + statistics.getStandardDeviation() + "\t" + statistics.getMax());
//  }
//
//  private GeneticProgrammingAlgorithm getAlg(double oF, double mP, double mR) {
//    Population[] population = new Population[1];
//    RandomUtil.temporaryWithSeed(54738, () ->
//      population[0] = new Population(30, () -> new RandomGenotype(trainingData))
//    );
//    return new GeneticProgrammingAlgorithm(
//      population[0],
//      trainingData.getFitnessFunction(),
//      new TournamentSelector(3, 0.75),
//      List.of(new SemanticRecombinator()),
////      List.of(new DefaultRecombinator()),
//      oF,
////      List.of(new AllMutator(), new InputsMutator(), new OperatorMutator()),
//      List.of(new SimpleSemanticMutator(), new AllMutator()),
//      mP,
//      mR
//    );
//  }
//
//}