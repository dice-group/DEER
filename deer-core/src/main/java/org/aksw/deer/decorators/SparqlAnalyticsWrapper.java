package org.aksw.deer.decorators;

import org.aksw.deer.DeerAnalyticsStore;
import org.aksw.deer.DeerExecutionGraphNode;
import org.aksw.deer.ParameterizedDeerExecutionGraphNode;
import org.aksw.deer.vocabulary.DEER;
import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.FaradayCageContext;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
@Extension
public class SparqlAnalyticsWrapper extends AbstractParameterizedDeerExecutionGraphNodeWrapper {

  private static final Logger logger = LoggerFactory.getLogger(SparqlAnalyticsWrapper.class);

  public static final Property SPARQL_SELECT_QUERY = DEER.property("sparqlSelectQuery");

  public static final Property JSON_OUTPUT = DEER.property("jsonOutput");

  @Override
  public @NotNull
  ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(SPARQL_SELECT_QUERY)
      .declareProperty(JSON_OUTPUT)
      .build();
  }

  private void applyTriggered(Resource id, List<Model> in, List<Model> out) {
    Dataset dataset = DatasetFactory.createGeneral();
    for (int i = 0; i < in.size(); i++) {
      dataset.addNamedModel(DEER.resource("inputGraph" + i).getURI(), in.get(i));
    }
    for (int i = 0; i < out.size(); i++) {
      dataset.addNamedModel(DEER.resource("outputGraph" + i).getURI(), out.get(i));
    }
    final String query = getParameterMap().get(SPARQL_SELECT_QUERY).asLiteral().getString();
    final String[] jsonOutput = { getParameterMap().get(JSON_OUTPUT).asLiteral().getString() };
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();
    List<String> resultVars = resultSet.getResultVars();
    resultSet.forEachRemaining(qs ->
      resultVars.stream()
        .filter(qs::contains)
        .forEach(varName ->
          jsonOutput[0] = jsonOutput[0].replace("?" + varName, "\"" + qs.get(varName).toString() + "\"")
        )
    );
    DeerAnalyticsStore.write(FaradayCageContext.getRunId(), id, new JSONObject(jsonOutput[0]));
    logger.info("AnalyticsWrapper {} keeping notes", getId());
  }

  @Override
  public DeerExecutionGraphNode wrap(DeerExecutionGraphNode executionNode) {
    if (executionNode instanceof Parameterized) {
      return new ParameterizedSparqlAnalyticsDecorator((ParameterizedDeerExecutionGraphNode) executionNode);
    } else {
      return new SparqlAnalyticsDecorator(executionNode);
    }
  }

  private class SparqlAnalyticsDecorator extends AbstractDeerExecutionGraphNodeDecorator {

    public SparqlAnalyticsDecorator(ExecutionGraphNode<Model> other) {
      super(other);
    }

    public List<Model> apply(List<Model> in) {
      List<Model> out = super.apply(in);
      applyTriggered(getWrapped().getId(), in, out);
      return out;
    }

  }

  private class ParameterizedSparqlAnalyticsDecorator extends AbstractParameterizedDeerExecutionGraphNodeDecorator {

    public ParameterizedSparqlAnalyticsDecorator(ParameterizedDeerExecutionGraphNode other) {
      super(other);
    }

    public List<Model> apply(List<Model> in) {
      List<Model> out = super.apply(in);
      applyTriggered(getWrapped().getId(), in, out);
      return out;
    }

  }

}