package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.FitnessFunction;
import org.aksw.deer.learning.RandomUtil;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.stream.IntStream;

/**
 *
 */
public class RandomGenotypeTest {

  private static TrainingData trainingData;
  private static String testDir = "./test/";
  private static String[] paths = new String[]{
    "a.ttl",
    "b.ttl",
    "c.ttl",
    "d.ttl",
    "e.ttl"
  };
  @BeforeClass
  public static void setUp() throws IOException {
    if (!Files.exists(Paths.get(testDir))) {
      Files.createDirectory(Paths.get(testDir));
    }
    Model model = ModelFactory.createDefaultModel().read(new StringReader(
      "@prefix ex: <http://example.org/>." +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
        "ex:s ex:p1 ex:o ." +
        "ex:s1 ex:p4 ex:o1 ." +
        "ex:s2 ex:p3 ex:o2 ." +
        "ex:o2 ex:p2 ex:o3 ." +
        "ex:s4 ex:p ex:o4 ." +
        "ex:s5 ex:p3 ex:o5 ." +
        "ex:s6 ex:p ex:o6 ." +
        "ex:o6 ex:p ex:o7 ." +
        "ex:s8 ex:p2 ex:o8 ." +
        "ex:s rdfs:comment \"Goethe lived in Leipzig.\"@en ." +
        "ex:s9 ex:p ex:o9 ."
    ), null, "TTL");
    Model target = ModelFactory.createDefaultModel().read(new StringReader(
      "@prefix ex: <http://example.org/>." +
        "@prefix ex2: <http://example2.org/>." +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
        "@prefix foxo:  <http://ns.aksw.org/fox/ontology#> ." +
        "ex2:s ex:p1 ex:o ." +
        "ex2:s1 ex:p4 ex:o1 ." +
        "ex2:s2 ex:p6 ex:o2 ." +
        "ex2:o2 ex:p2 ex:o3 ." +
        "ex2:s4 ex:p ex:o4 ." +
        "ex2:s5 ex:p6 ex:o5 ." +
        "ex2:s6 ex:p ex:o6 ." +
        "ex2:o6 ex:p ex:o7 ." +
        "ex2:s8 ex:p2 ex:o8 ." +
        "ex2:s rdfs:comment \"Goethe lived in Leipzig.\"@en ." +
        "ex2:s foxo:relatedTo <http://dbpedia.org/resource/Johann_Wolfgang_von_Goethe> , <http://dbpedia.org/resource/Leipzig> ." +
        "ex2:s9 ex:p ex:o9 ."
    ), null, "TTL");
    model.write(new FileWriter(testDir + paths[0]), "TTL");
    model.write(new FileWriter(testDir + paths[1]), "TTL");
    target.write(new FileWriter(testDir + paths[2]), "TTL");
    target.write(new FileWriter(testDir + paths[3]), "TTL");
    trainingData = new TrainingData(List.of(testDir + paths[0]), List.of(testDir + paths[1]), testDir + paths[2], testDir + paths[3], testDir + paths[4]);
    Genotype.SIZE = 7;
  }

  @AfterClass
  public static void tearDown() throws IOException {
    if (Files.exists(Paths.get(testDir))) {
      FileUtils.deleteDirectory(Paths.get(testDir).toFile());
    }
  }

  @Test
  public void simpleTest() {
    List<PopulationEvaluationResult> evaluationResults = getAlg(0, 0.75, 0.25).run();
    System.out.println(evaluationResults.size());
    System.out.println(Phenotype.of(evaluationResults.get(evaluationResults.size()-1).getBest()));
    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().compactBestResult(false, 0));
    System.out.println(evaluationResults.get(evaluationResults.size()-1).getBest().getBestFitness());
  }

  @Test
  public void constructorTest() {
    for (double oF = 0; oF <= 1; oF+=.1) {
      for (double mP = 0.1; mP <= 1; mP+=.2) {
        for (double mR = 0.1; mR <= 1; mR+=.2) {
          runSimpleExperiment(oF, mP, mR);
        }
      }
    }
  }

  private void runSimpleExperiment(double oF, double mP, double mR) {
    PopulationEvaluationResult.DoubleStatistics statistics = IntStream.range(0, 10)
      .mapToDouble(j -> getAlg(oF, mP, mR).run().size())
      .collect(PopulationEvaluationResult.DoubleStatistics::new,
        PopulationEvaluationResult.DoubleStatistics::accept,
        PopulationEvaluationResult.DoubleStatistics::combine);
    System.out.println(String.format(Locale.ENGLISH, "%.2f \t %.2f \t %.2f \t %.2f \t %.2f \t %.2f", oF, mP, mR, statistics.getAverage(), statistics.getStandardDeviation(), statistics.getMax()));
//    System.out.println(oF  + "\t" +  mP  + "\t" +  mR + "\t" + statistics.getAverage() + "\t" + statistics.getStandardDeviation() + "\t" + statistics.getMax());
  }

  private GeneticProgrammingAlgorithm getAlg(double oF, double mP, double mR) {
    Population[] population = new Population[1];
    RandomUtil.temporaryWithSeed(54738, () ->
      population[0] = new Population(30, () -> new RandomGenotype(trainingData))
      );
    return new GeneticProgrammingAlgorithm(
      population[0],
      new FitnessFunction(new int[]{1,1,1,1}, 2),
      new TournamentSelector(4, 0.9),
      List.of(new DefaultRecombinator()),
      oF,
      List.of(new AllMutator(), new OperatorMutator(), new InputsMutator()),
      mP,
      mR
    );
  }

}