package org.aksw.deer.learning.genetic;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 *
 */
public class EvaluationData {

  private List<Model> sources;
  private List<Model> targets;

  private static class EvaluationResult {

    private double[] recall = new double[4];
    private double[] precision = new double[4];
    private double[] fmeasure = new double[4];

    private EvaluationResult(Model source, Model target) {
      this(source, target, 1.0d);
    }

    private EvaluationResult(Model source, Model target, double beta) {
      Function<Model, Set<?>> s, p, o, t;
      s = m -> m.listSubjects().filterDrop(RDFNode::isAnon).toSet();
      p = m -> m.listStatements().mapWith(Statement::getPredicate).toSet();
      o = m -> m.listObjects().filterDrop(RDFNode::isAnon).toSet();
      t = m -> m.listStatements().filterDrop(stmt -> stmt.getObject().isAnon() || stmt.getSubject().isAnon()).toSet();
      computePR(0, s.apply(source), s.apply(target));
      computePR(0, p.apply(source), p.apply(target));
      computePR(0, o.apply(source), o.apply(target));
      computePR(0, t.apply(source), t.apply(target));
      for (int i = 0; i < fmeasure.length; i++) {
        fmeasure[i] = (precision[i] * recall[i]) * (Math.pow(beta, 2.0d) * precision[i] + recall[i]);
      }
    }

    private void computePR(int i, Set<?> s, Set<?> t) {
      long truePositives = s.stream().filter(t::contains).count();
      precision[i] = ((double)truePositives / s.size());
      recall[i] = ((double)truePositives / t.size());
    }

  }

  public EvaluationData(List<Model> sources, List<Model> targets) {
    this.sources = sources;
    this.targets = targets;
  }
//
//  public EvaluationResult evaluate() {
//
//  }

}