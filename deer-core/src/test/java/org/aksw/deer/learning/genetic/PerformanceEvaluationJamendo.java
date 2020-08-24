package org.aksw.deer.learning.genetic;

import com.google.common.util.concurrent.AtomicDouble;
import org.aksw.deer.enrichments.LinkingEnrichmentOperator;
import org.aksw.deer.learning.FitnessFunction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 *
 */
public class PerformanceEvaluationJamendo {

  private static TrainingData trainingData;
  private static String testDir = "./test/";
  private static String[] paths = new String[]{
    "a.ttl",
    "b.ttl",
    "c.ttl",
    "d.ttl"
  };

  @BeforeClass
  public static void setUp() throws IOException {
    LinkingEnrichmentOperator.setStaticLearning(()-> new LinkingEnrichmentOperator().createParameterMap()
      .add(LinkingEnrichmentOperator.LINKING_PREDICATE, ResourceFactory.createProperty(""))
      .add(LinkingEnrichmentOperator.LINK_SPECIFICATION, ResourceFactory.createStringLiteral("exactmatch(x.http://dbpedia.org/ontology/birthPlace, y.http://www.w3.org/2000/01/rdf-schema#label)"))
      .add(LinkingEnrichmentOperator.THRESHOLD, ResourceFactory.createTypedLiteral(1.0))
      .init());

    if (!Files.exists(Paths.get(testDir))) {
      Files.createDirectory(Paths.get(testDir));
    }

    Model target = ModelFactory.createDefaultModel().read(new StringReader(
        "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Tom_Morello> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Marcus_Martin> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/name> \"ARCTIC\" .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/mo/MusicArtist> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Yellowknife> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/homepage> <http://www.projectarctic.com> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Northwest_Territories> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://www.w3.org/2000/01/rdf-schema#comment> \"<p>ARCTIC - the band and/or solo project of Marcus Martin - is all about duality. His childhood in Yellowknife, a small gold mining town in the Arctic region of Canada's Northwest Territories, was a stark contrast to his experiences in a private UK boarding school. He grew up in the land of lakes, trees, rocks and ice with true wilderness spirit and is equally at home sharing his ARCTIC adventures in any big city, where there are more people on one street than in his whole hometown. </p>\\n<p>Coming from the Tom Morello school of guitar texture, he has learned to challenge the listener by layering unique sounds usually found only in the electric guitar world. As a loop-based acoustic artist he tours with an 80 lb. effects pedalboard - not traveling light compared to most acoustic singer-songwriters. </p>\\n<p>His live set is where this duality takes flight, as he constructs each song before the audience, piece by piece. Influenced by both 70's progressive rock and conventional songwriters of the day, ARCTIC bridges a world of opposites together. He layers haunting melodies on top of his arrangements, improvising like a jazz artist, intertwining vocals and guitar into a chilled and airy soundscape. </p>\" .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/United_Kingdom> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://ns.aksw.org/fox/ontology#relatedTo> <http://dbpedia.org/resource/Canada> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/img> <http://img.jamendo.com/artists/a/arctic.gif> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/made> <http://dbtune.org/jamendo/record/2357> .\n" +
          "<http://dbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/based_near> <http://sws.geonames.org/6251999/> .\n" +
          ""), null, "NT");
// (a -> )(b -> AC ->) -> MERGE -> NER -> PC -> DEREF
    Model a = ModelFactory.createDefaultModel().read(new StringReader(
      "<http://adbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/name> \"ARCTIC\" .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/made> <http://dbtune.org/jamendo/record/2357> .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/img> <http://img.jamendo.com/artists/a/arctic.gif> .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/homepage> <http://www.projectarctic.com> .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://xmlns.com/foaf/0.1/based_near> <http://sws.geonames.org/6251999/> .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://www.w3.org/2000/01/rdf-schema#comment> \"<p>ARCTIC - the band and/or solo project of Marcus Martin - is all about duality. His childhood in Yellowknife, a small gold mining town in the Arctic region of Canada's Northwest Territories, was a stark contrast to his experiences in a private UK boarding school. He grew up in the land of lakes, trees, rocks and ice with true wilderness spirit and is equally at home sharing his ARCTIC adventures in any big city, where there are more people on one street than in his whole hometown. </p>\\n<p>Coming from the Tom Morello school of guitar texture, he has learned to challenge the listener by layering unique sounds usually found only in the electric guitar world. As a loop-based acoustic artist he tours with an 80 lb. effects pedalboard - not traveling light compared to most acoustic singer-songwriters. </p>\\n<p>His live set is where this duality takes flight, as he constructs each song before the audience, piece by piece. Influenced by both 70's progressive rock and conventional songwriters of the day, ARCTIC bridges a world of opposites together. He layers haunting melodies on top of his arrangements, improvising like a jazz artist, intertwining vocals and guitar into a chilled and airy soundscape. </p>\" .\n" +
        "<http://adbtune.org/jamendo/artist/1399> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/mo/MusicArtist> .\n" +
        ""), null, "NT");
    a.write(new FileWriter(testDir + paths[0]), "TTL");
    target.write(new FileWriter(testDir + paths[2]), "TTL");
    trainingData = new TrainingData(new FitnessFunction(new int[]{1,1,1,1}, 1), List.of(testDir + paths[0]), List.of(testDir + paths[0]), testDir + paths[2], testDir + paths[2], testDir + paths[3]);
    Genotype.SIZE = 9;
  }

  private static double calcMeanCI(SummaryStatistics stats, double level) {
    try {
      // Create T Distribution with N-1 degrees of freedom
      TDistribution tDist = new TDistribution(stats.getN() - 1);
      // Calculate critical value
      double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
      // Calculate confidence interval
      return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
    } catch (MathIllegalArgumentException e) {
      return Double.NaN;
    }
  }

  @AfterClass
  public static void tearDown() throws IOException {
    if (Files.exists(Paths.get(testDir))) {
      FileUtils.deleteDirectory(Paths.get(testDir).toFile());
    }
  }

  @Test
  public void simpleTest() {
    List<PopulationEvaluationResult> evaluationResults = getAlg(1.0, 0.5, 0.25).run();
    System.out.println(evaluationResults.size());
    Phenotype of = Phenotype.of(evaluationResults.get(evaluationResults.size() - 1).getBest());
    Genotype genotype = evaluationResults.get(evaluationResults.size() - 1).getBest().compactBestResult(false, 0);
    System.out.println(of);
    System.out.println(genotype);
    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().getBestFitness());
  }

  @Test
  public void constructorTest() {
    int i=0;while(++i<=10) {
      System.out.println(i + "-------------");
      runPerformanceExperiment(1.0, 0.5, 0.25);
    }
  }

  private void runPerformanceExperiment(double oF, double mP, double mR) {
    int it = 50;
    double[][] results = new double[it][0];
    double maxGenGlobal = 0;
    for (int j = 0; j < it; j++) {
      results[j] = getAlg(oF, mP, mR).run().stream()
        .mapToDouble(PopulationEvaluationResult::getMax).toArray();
      if (results[j].length > maxGenGlobal) {
        maxGenGlobal = results[j].length;
      }
    }

    for (int g = 0; g < maxGenGlobal; g++) {
      SummaryStatistics stats = new SummaryStatistics();
      for (int j = 0; j < it; j++) {
        if (g < results[j].length ) {
          stats.addValue(results[j][g]);
        } else {
          stats.addValue(results[j][results[j].length-1]);
        }
      }
      // Calculate 95% confidence interval
      double ci = calcMeanCI(stats, 0.95);
      double lower = stats.getMax();
      double upper = stats.getMin();
      System.out.println(String.format("%d %.6f %.6f %.6f %.6f", g, stats.getMean(), ci, lower, upper));
    }




//    System.out.println(String.format(Locale.ENGLISH, "%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", oF, mP, mR, statistics.getAverage(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax(), d.get()/it, i.get()));
  }

  private void runSimpleExperiment(double oF, double mP, double mR) {
    int it = 1;
    AtomicInteger i = new AtomicInteger(0);
    AtomicDouble d = new AtomicDouble(0);
    PopulationEvaluationResult.DoubleStatistics statistics = IntStream.range(0, it)
      .mapToDouble(j -> {
        GeneticProgrammingAlgorithm alg = getAlg(oF, mP, mR);
        List<PopulationEvaluationResult> evaluationResults = alg.run();
        double max = evaluationResults.get(evaluationResults.size() - 1).getMax();
        d.addAndGet(max);
        if (max == 1.0) {
          i.incrementAndGet();
        }
        return evaluationResults.size();
      })
      .collect(PopulationEvaluationResult.DoubleStatistics::new,
        PopulationEvaluationResult.DoubleStatistics::accept,
        PopulationEvaluationResult.DoubleStatistics::combine);
    System.out.println(String.format(Locale.ENGLISH, "%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", oF, mP, mR, statistics.getAverage(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax(), d.get()/it, i.get()));
//    System.out.println(oF  + "\t" +  mP  + "\t" +  mR + "\t" + statistics.getAverage() + "\t" + statistics.getStandardDeviation() + "\t" + statistics.getMax());
  }

  private GeneticProgrammingAlgorithm getAlg(double oF, double mP, double mR) {
    return new GeneticProgrammingAlgorithm(
      new Population(30, () -> new RandomGenotype(trainingData)),
      trainingData.getFitnessFunction(),
      new TournamentSelector(3, 0.75),
      List.of(new SemanticRecombinator()),
      oF,
      List.of(new SimpleSemanticMutator(), new AllMutator()),
      mP,
      mR
    );
  }

}