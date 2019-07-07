package org.aksw.deer.enrichments;

import org.aksw.deer.learning.ReverseLearnable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.pf4j.Extension;

import java.util.List;

@Extension
public class MergeEnrichmentOperator extends AbstractEnrichmentOperator implements ReverseLearnable {

  @Override
  protected List<Model> safeApply(List<Model> models) {
    Model merge = ModelFactory.createDefaultModel();
    for (Model model : models) {
      merge.add(model);
    }
    return List.of(merge);
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(2,Integer.MAX_VALUE,1,1);
  }

  @Override
  public double predictApplicability(List<Model> inputs, Model target) {
    Model sI = inputs.get(0);
    Model tI = inputs.get(1);
    // input has size 1 -> 0.05
    if (inputs.size() == 1) {
      return 0;
    }
    double[] score = new double[2];
    int size = target.listStatements()
      .filterDrop(stmt -> stmt.getObject().isAnon() && stmt.getSubject().isAnon())
      .mapWith(stmt -> {
        boolean sInS = sI.containsResource(stmt.getSubject());
        boolean sInT = tI.containsResource(stmt.getSubject());
        boolean oInS = sI.containsResource(stmt.getObject());
        boolean oInT = tI.containsResource(stmt.getObject());
        score[0] += getScore(sInS, sInT, oInS, oInT);
        score[1] += getScore(sInT, sInS, oInT, oInS);
        return stmt;
      }).toList().size();
    return (score[0]+score[1])/size;
  }

  private double getScore(boolean sInS, boolean sInT, boolean oInS, boolean oInT) {
    if (sInS && oInS && !sInT && !oInT) {
      return 1.0;
    } else if ((sInS || oInS) && !sInT && !oInT) {
      return 0.75;
    } else if (sInS && oInS) {
      return 0.5;
    } else if (sInS || oInS) {
      return 0.25;
    }
    return 0;
  }

  @Override
  public List<Model> reverseApply(List<Model> inputs, Model target) {
    Model sI = inputs.get(0);
    Model tI = inputs.get(1);
    Model s = ModelFactory.createDefaultModel();
    Model t = ModelFactory.createDefaultModel();
    target.listStatements()
      .filterDrop(stmt -> stmt.getObject().isAnon() && stmt.getSubject().isAnon())
      .forEachRemaining(stmt -> {
        boolean sInS = sI.containsResource(stmt.getSubject());
        boolean sInT = tI.containsResource(stmt.getSubject());
        boolean oInS = sI.containsResource(stmt.getObject());
        boolean oInT = tI.containsResource(stmt.getObject());
        double inSScore = getScore(sInS, sInT, oInS, oInT);
        double inTScore = getScore(sInT, sInS, oInT, oInS);
        if (inSScore >= inTScore) {
          s.add(stmt);
        }
        if (inTScore >= inSScore) {
          t.add(stmt);
        }
      });
    return List.of(s, t);
  }

  @Override
  public DegreeBounds getLearnableDegreeBounds() {
    return new DegreeBounds(2,2,1,1);
  }
}