package org.aksw.deer.operator.merge;

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
public class MergeOperator implements IOperator {

  private static final Logger logger = Logger.getLogger(MergeOperator.class.getName());

  @Override
  public List<Model> process(final List<Model> models, final Map<String, String> parameters) {
    logger.info("--------------- Merge Operator ---------------");
    List<Model> result = new ArrayList<>();
    Model merge = ModelFactory.createDefaultModel();
    for (Model model : models) {
      merge.add(model);
    }
    result.add(merge);
    return result;
  }

  public List<String> getParameters() {
    return new ArrayList<>();
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