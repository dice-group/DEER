package org.aksw.deer.operator.clone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aksw.deer.util.IOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
public class CloneOperator implements IOperator {

  private static final Logger logger = Logger.getLogger(CloneOperator.class.getName());
  public int clonesCount = 2;

  @Override
  public List<Model> process(final List<Model> models, final Map<String, String> parameters) {
    logger.info("--------------- Clone Operator ---------------");
    if (parameters != null && parameters.containsKey("cloneCount")) {
      clonesCount = Integer.parseInt(parameters.get("cloneCount"));
    }
    List<Model> result = new ArrayList<>();
    for (int i = 0; i < clonesCount; i++) {
      Model clone = ModelFactory.createDefaultModel();
      clone.add(models.get(0));
      result.add(clone);
    }
    return result;
  }

  public List<String> getParameters() {
    List<String> parameters = new ArrayList<>();
    parameters.add("cloneCount");
    return parameters;
  }

  @Override
  public List<String> getNecessaryParameters() {
    return new ArrayList<>();
  }

  @Override
  public String id() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Resource getType() {
    return null;
  }

}
