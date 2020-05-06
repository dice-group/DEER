//package org.aksw.deer.learning.genetic;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import org.aksw.deer.enrichments.LinkingEnrichmentOperator;
//import org.aksw.deer.learning.FitnessFunction;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.math3.distribution.TDistribution;
//import org.apache.commons.math3.exception.MathIllegalArgumentException;
//import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.rdf.model.ResourceFactory;
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
//public class HPOPhase6 {
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
//    LinkingEnrichmentOperator.setStaticLearning(()-> new LinkingEnrichmentOperator().createParameterMap()
//      .add(LinkingEnrichmentOperator.LINKING_PREDICATE, ResourceFactory.createProperty("<http://my.dataset.edu/birthPlace>"))
//      .add(LinkingEnrichmentOperator.LINK_SPECIFICATION, ResourceFactory.createStringLiteral("exactmatch(x.http://dbpedia.org/ontology/birthPlace, y.http://www.w3.org/2000/01/rdf-schema#label)"))
//      .add(LinkingEnrichmentOperator.THRESHOLD, ResourceFactory.createTypedLiteral(1.0))
//      .init());
//
//    if (!Files.exists(Paths.get(testDir))) {
//      Files.createDirectory(Paths.get(testDir));
//    }
//
//    Model a = ModelFactory.createDefaultModel().read(new StringReader(
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/birthDate> \"1947-0-0\"^^<http://www.w3.org/2001/XMLSchema#date> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://xmlns.com/foaf/0.1/name> \"Peter Koper\"@en .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/birthPlace> \"Quakenbrück\" .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://www.w3.org/2000/01/rdf-schema#comment> \"Peter Koper (born 1947) is an American journalist, professor, screenwriter, and producer. He numbers among the original Dreamlanders, the group of actors and artists who worked with independent film maker John Waters on his early films. He has written for the Associated Press, the Baltimore Sun, American Film, Rolling Stone, and People. He worked as a staff writer and producer for America's Most Wanted, and has written television for the Discovery Channel, the Learning Channel, Paramount Television and Lorimar Television. Koper wrote and co-produced the cult movie Headless Body in Topless Bar, and wrote the screenplay for Island of the Dead. He has taught at the University of the District of Columbia, and Hofstra University.\" .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/producerOf> <http://dbpedia.org/resource/Island_of_the_Dead_(2000_film)> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/almaMater> <http://dbpedia.org/resource/Johns_Hopkins_University> .\n" +
//        ""), null, "NT");
//
//    Model b = ModelFactory.createDefaultModel().read(new StringReader(
//      "<http://example.org/Quakenbrück> <http://dbpedia.org/ontology/postalCode> \"49610\" .\n" +
//        "<http://example.org/Quakenbrück> <http://dbpedia.org/ontology/country> <http://dbpedia.org/resource/Germany> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2003/01/geo/wgs84_pos#long> \"7.9574999809265136719\"^^<http://www.w3.org/2001/XMLSchema#float> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> \"52.67722320556640625\"^^<http://www.w3.org/2001/XMLSchema#float> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2000/01/rdf-schema#label> \"Quakenbrück\" .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Town> ." +
//        ""), null, "NT");
//// (a -> )(b -> AC ->) -> MERGE -> NER -> PC -> DEREF
//    Model target = ModelFactory.createDefaultModel().read(new StringReader(
//      "<http://dbpedia.org/resource/Island_of_the_Dead_(2000_film)> <http://dbpedia.org/ontology/director> <http://dbpedia.org/resource/Tim_Southam> .\n" +
//      "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Baltimore> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://my.dataset.edu/birthPlace> <http://example.org/Quakenbrück> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Koper> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Peter_Griffin> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/birthDate> \"1947-0-0\"^^<http://www.w3.org/2001/XMLSchema#date> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/ISLAND> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Associated_Press> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Paramount_Pictures> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Rock_%28geology%29> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Discovery_Channel> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Washington%2C_D.C.> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/birthPlace> \"Quakenbrück\" .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/John_Waters> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://xmlns.com/foaf/0.1/name> \"Peter Koper\"@en .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://www.w3.org/2000/01/rdf-schema#comment> \"Peter Koper (born 1947) is an American journalist, professor, screenwriter, and producer. He numbers among the original Dreamlanders, the group of actors and artists who worked with independent film maker John Waters on his early films. He has written for the Associated Press, the Baltimore Sun, American Film, Rolling Stone, and People. He worked as a staff writer and producer for America's Most Wanted, and has written television for the Discovery Channel, the Learning Channel, Paramount Television and Lorimar Television. Koper wrote and co-produced the cult movie Headless Body in Topless Bar, and wrote the screenplay for Island of the Dead. He has taught at the University of the District of Columbia, and Hofstra University.\" .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Hofstra_University> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/producerOf> <http://dbpedia.org/resource/Island_of_the_Dead_(2000_film)> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://dbpedia.org/ontology/almaMater> <http://dbpedia.org/resource/Johns_Hopkins_University> .\n" +
//        "<http://dbpedia.org/resource/Peter_Koper> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Sun> .\n" +
//        "<http://example.org/Quakenbrück> <http://dbpedia.org/ontology/postalCode> \"49610\" .\n" +
//        "<http://example.org/Quakenbrück> <http://dbpedia.org/ontology/country> <http://dbpedia.org/resource/Germany> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2003/01/geo/wgs84_pos#long> \"7.9574999809265136719\"^^<http://www.w3.org/2001/XMLSchema#float> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> \"52.67722320556640625\"^^<http://www.w3.org/2001/XMLSchema#float> .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/2000/01/rdf-schema#label> \"Quakenbrück\" .\n" +
//        "<http://example.org/Quakenbrück> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Town> .\n" +
//        ""
//    ), null, "NT");
//    a.write(new FileWriter(testDir + paths[0]), "TTL");
//    b.write(new FileWriter(testDir + paths[1]), "TTL");
//    target.write(new FileWriter(testDir + paths[2]), "TTL");
//    trainingData = new TrainingData(new FitnessFunction(new int[]{1,1,1,1}, 1), List.of(testDir + paths[0], testDir + paths[1]), List.of(testDir + paths[0], testDir + paths[1]), testDir + paths[2], testDir + paths[2], testDir + paths[3]);
//    Genotype.SIZE = 10;
//  }
//
//  private static double calcMeanCI(SummaryStatistics stats, double level) {
//    try {
//      // Create T Distribution with N-1 degrees of freedom
//      TDistribution tDist = new TDistribution(stats.getN() - 1);
//      // Calculate critical value
//      double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
//      // Calculate confidence interval
//      return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
//    } catch (MathIllegalArgumentException e) {
//      return Double.NaN;
//    }
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
//    List<PopulationEvaluationResult> evaluationResults = getAlg(1.0, 0.5, 0.5).run();
//    System.out.println(evaluationResults.size());
//    Phenotype of = Phenotype.of(evaluationResults.get(evaluationResults.size() - 1).getBest());
//    Genotype genotype = evaluationResults.get(evaluationResults.size() - 1).getBest().compactBestResult(false, 0);
//    System.out.println(of);
//    System.out.println(genotype);
//    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().getBestFitness());
//  }
//
//  @Test
//  public void constructorTest() {
//    int i = 0;
//    while (++i < 10) {
//      System.out.println(i + "------ " + i + " ------" + i);
//      runPerformanceExperiment(1.0, 0.5, 0.25);
//    }
//  }
//
//  private void runPerformanceExperiment(double oF, double mP, double mR) {
//    int it = 50;
//    double[][] results = new double[it][0];
//    double maxGenGlobal = 0;
//    for (int j = 0; j < it; j++) {
//      results[j] = getAlg(oF, mP, mR).run().stream()
//        .mapToDouble(PopulationEvaluationResult::getMax).toArray();
//      if (results[j].length > maxGenGlobal) {
//        maxGenGlobal = results[j].length;
//      }
//    }
//
//    for (int g = 0; g < maxGenGlobal; g++) {
//      SummaryStatistics stats = new SummaryStatistics();
//      for (int j = 0; j < it; j++) {
//        if (g < results[j].length ) {
//          stats.addValue(results[j][g]);
//        } else {
//          stats.addValue(results[j][results[j].length-1]);
//        }
//      }
//      // Calculate 95% confidence interval
//      double ci = calcMeanCI(stats, 0.95);
//      double lower = stats.getMax();
//      double upper = stats.getMin();
//      System.out.println(String.format("%d %.6f %.6f %.6f %.6f", g, stats.getMean(), ci, lower, upper));
//    }
//
//
//
//
////    System.out.println(String.format(Locale.ENGLISH, "%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", oF, mP, mR, statistics.getAverage(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax(), d.get()/it, i.get()));
//  }
//
//  private void runSimpleExperiment(double oF, double mP, double mR) {
//    int it = 1;
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
//    return new GeneticProgrammingAlgorithm(
//      new Population(30, () -> new RandomGenotype(trainingData)),
//      trainingData.getFitnessFunction(),
//      new TournamentSelector(3, 0.75),
//      List.of(new SemanticRecombinator()),
//      oF,
//      List.of(new SimpleSemanticMutator(), new AllMutator()),
//      mP,
//      mR
//    );
//  }
//
//}