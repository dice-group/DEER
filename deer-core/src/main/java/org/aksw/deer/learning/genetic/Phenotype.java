package org.aksw.deer.learning.genetic;

import org.aksw.deer.ParameterizedDeerExecutionNode;
import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.faraday_cage.engine.ExecutionGraph;
import org.aksw.faraday_cage.util.ExecutionGraphSerializer;
import org.apache.jena.rdf.model.Model;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
class Phenotype extends ExecutionGraph<Model> {

  static Phenotype of(Genotype genotype) {
    return new Phenotype(genotype);
  }

  private Phenotype(Genotype geno) {
    super(geno.getSize());
    NavigableSet<Short> relevantRows = new TreeSet<>();
    relevantRows.add(geno.bestResultRow);
    for(short i = geno.bestResultRow; i >= 0; i--) {
      if (relevantRows.contains(i)) {
        short[] e = geno.getRow(i);
        for (int j = 0; j < e[0]; j++) {
          relevantRows.add(e[2 + j * 2]);
        }
      }
    }
    Map<Short, Short> skipMap = new HashMap<>();
    // strip no ops
    List<Model> trainingSources = geno.trainingData.getTrainingSources();
    Function<Short, Model> getResultModel = i -> {
      if (i < trainingSources.size()) {
        return trainingSources.get(i);
      }
      return geno.results[i].getResultModel();
    };
    for (short i : relevantRows) {
      if (i >= geno.getInputSize()) { //ignore readers
        short[] row = geno.getRow(i);
        short arity = row[0];
        for (short j = 0; j < arity; j++) {
          short input = row[2+j*2];
          if (skipMap.containsKey(input)) {
            input = skipMap.get(input);
          }
          if (getResultModel.apply(input).isIsomorphicWith(getResultModel.apply(i))) {
            // no op
            skipMap.put(i, input);
            break;
          }
        }
      }
    }
    this.entries = new short[relevantRows.size()+1-skipMap.size()][2];
    this.ops = new ArrayList<>(entries.length);
    for (int i = 0; i < entries.length; i++) ops.add(null);
    short k = 0;
    List<ParameterizedDeerExecutionNode> readers = geno.trainingData.getEvaluationReaders();
    for (short i : relevantRows) {
      if (k < readers.size()) {
        addRow(k, readers.get(k++), geno.getRow(i));
      } else if (!skipMap.containsKey(i)) {
        short[] row = geno.getRow(i);
        short arity = row[0];
        short[] nRow = Arrays.copyOf(row, row.length);
        for (short j = 0; j < arity; j++) {
          if (skipMap.containsKey(nRow[2+2*j])) {
            nRow[2+2*j] = skipMap.get(nRow[2+2*j]);
          }
        }
        addRow(k++, RandomOperatorFactory.reproduce((EnrichmentOperator)geno.getRawNode(i)), nRow);
      }
    }
    addRow(k, geno.trainingData.getResultWriter(), new short[]{1, 0, k, 0});
  }

  public Model toModel() {
    return ExecutionGraphSerializer.serialize(this);
  }

}
