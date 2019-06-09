package org.aksw.deer.learning.genetic.io;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 */
public class LinearLearningData {

  private Model model;
  private List<Pair<Model, Model>> entries;

  LinearLearningData(Model model, String restriction, int depth) {
    this.model = model;


  }
}

/*
  public void forEachSample(Consumer<Pair<>>) {

  }

  public <U> Stream<U> map(Function<LinearLearningCBD, U> f) {
    return entries.stream().map(f);
  }

  public Model getModel() {
    return model;
  }


}*/
