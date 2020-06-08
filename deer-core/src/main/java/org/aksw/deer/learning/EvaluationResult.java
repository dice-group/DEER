package org.aksw.deer.learning;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import java.util.Set;
import java.util.function.Function;

/**
 *
 */
public class EvaluationResult {

  public static final String HEADER = "precision_subjects, precision_predicates, precision_objects, precision_triples, recall_subjects, recall_predicates, recall_objects, recall_triples";

  private double[] recall = new double[4];
  private double[] precision = new double[4];
  private Model selected;

  public EvaluationResult(Model selected, Model relevant) {
    this.selected = selected;
    Function<Model, Set<?>> s, p, o, t;
    s = m -> m.listSubjects().filterDrop(RDFNode::isAnon).toSet();
    p = m -> m.listStatements().mapWith(Statement::getPredicate).toSet();
    o = m -> m.listObjects().filterDrop(RDFNode::isAnon).toSet();
    t = m -> m.listStatements().filterDrop(stmt -> stmt.getObject().isAnon() || stmt.getSubject().isAnon()).toSet();
    computePR(0, s.apply(selected), s.apply(relevant));
    computePR(1, p.apply(selected), p.apply(relevant));
    computePR(2, o.apply(selected), o.apply(relevant));
    computePR(3, t.apply(selected), t.apply(relevant));
  }

  private void computePR(int i, Set<?> s, Set<?> t) {
    long truePositives = s.stream().filter(t::contains).count();
    precision[i] = ((double)truePositives / s.size());
    recall[i] = ((double)truePositives / t.size());
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    toStringHelper(sb, precision);
    toStringHelper(sb, recall);
    sb.deleteCharAt(sb.length()-1);
    return sb.toString();
  }

  private void toStringHelper(StringBuilder sb, double[] evaluationMetric) {
    sb.append(evaluationMetric[0]);
    sb.append(",");
    sb.append(evaluationMetric[1]);
    sb.append(",");
    sb.append(evaluationMetric[2]);
    sb.append(",");
    sb.append(evaluationMetric[3]);
    sb.append(",");
  }

  public double[] getIndividualFMeasures(double beta) {
    double[] result = new double[4];
    double alpha = Math.pow(beta, 2.0d);
    for (int i = 0; i < result.length; i++) {
      if (precision[i] + recall[i] == 0) {
        continue;
      }
      result[i] = (1 + alpha) * (precision[i] * recall[i]) / (alpha * precision[i] + recall[i]);
    }
    return result;
  }

  public Model getResultModel() {
    return selected;
  }

  public double[] getIndividualRecall() {
    return recall;
  }

  public double[] getIndividualPrecision() {
    return precision;
  }

}
