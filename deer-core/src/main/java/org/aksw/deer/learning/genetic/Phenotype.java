package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.EvaluationResult;
import org.aksw.faraday_cage.engine.CompiledExecutionGraph;
import org.aksw.faraday_cage.engine.ExecutionGraph;
import org.aksw.faraday_cage.util.ExecutionGraphSerializer;
import org.apache.jena.rdf.model.Model;

/**
 *
 */
class Phenotype extends ExecutionGraph<Model> {

  private EvaluationResult result = null;

  static Phenotype of(Genotype genotype) {
    return new Phenotype(genotype.compactBestResult(true, 0));
  }

  private Phenotype(Genotype g) {
    super(g.getSize()+1);
    for (int i = 0; i < getSize()-1; i++) {
      addRow(i, g.getRawNode(i), g.getRow(i));
    }
    addRow(getSize()-1,
      g.trainingData.getResultWriter(m -> {
        result = new EvaluationResult(m, g.trainingData.getEvaluationTarget());
      }),
      new int[]{1, 0, getSize()-2, 0});
  }

  public EvaluationResult getResult() {
    if (result == null) {
      CompiledExecutionGraph of = CompiledExecutionGraph.of(this);
      of.join();
    }
    return result;
  }

  public Model toModel() {
    return ExecutionGraphSerializer.serialize(this);
  }

}