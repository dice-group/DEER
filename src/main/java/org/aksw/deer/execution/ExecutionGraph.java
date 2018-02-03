package org.aksw.deer.execution;

import static org.aksw.deer.util.QueryHelper.forEachResultOf;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 */
public class ExecutionGraph {

  private Map<Resource, List<Resource>> operatorInputs;
  private Map<Resource, List<Resource>> operatorOutputs;
  private Map<Resource, List<Resource>> datasetProducers;
  private Map<Resource, List<Resource>> datasetConsumers;
  private Map<Resource, Integer> subGraphIds;
  private Set<Resource> visitedOperators;
  private Model model;

  public ExecutionGraph(Model model) {
    this.operatorInputs = new HashMap<>();
    this.operatorOutputs = new HashMap<>();
    this.datasetConsumers = new HashMap<>();
    this.datasetProducers = new HashMap<>();
    this.subGraphIds = new HashMap<>();
    this.visitedOperators = new HashSet<>();
    this.model = model;
    this.fill();
  }

  private void fill() {
    SelectBuilder sb = new SelectBuilder().setDistinct(true).addVar("?op").addVar("?list");
    forEachResultOf(sb.clone().addWhere("?op", DEER.hasInput, "?list").build(), model,
      (qs) -> fillFromList(operatorInputs, datasetConsumers, qs.getResource("?op"), qs.getResource("?list").asResource()));
    forEachResultOf(sb.clone().addWhere("?op", DEER.hasOutput, "?list").build(), model,
      (qs) -> fillFromList(operatorOutputs, datasetProducers, qs.getResource("?op"), qs.getResource("?list").asResource()));
  }


  private void fillFromList(Map<Resource, List<Resource>> map, Map<Resource, List<Resource>> reverseMap, Resource op, Resource listBNode) {
    List<Resource> list = new ArrayList<>();
    RDFList rdfList = listBNode.as(RDFList.class);
    ExtendedIterator<RDFNode> items = rdfList.iterator();
    while (items.hasNext()) {
      Resource ds = items.next().asResource();
      list.add(ds);
      if (!reverseMap.containsKey(ds)) {
        reverseMap.put(ds, new ArrayList<>());
      }
      reverseMap.get(ds).add(op);
    }
    map.put(op, list);
  }

  public List<Resource> getOperatorInputs(Resource op) {
    return operatorInputs.getOrDefault(op, Collections.emptyList());
  }

  public List<Resource> getOperatorOutputs(Resource op) {
    return operatorOutputs.getOrDefault(op, Collections.emptyList());
  }

  public List<Resource> getDatasetProducers(Resource ds) {
    return datasetProducers.getOrDefault(ds, Collections.emptyList());
  }

  public List<Resource> getDatasetConsumers(Resource ds) {
    return datasetConsumers.getOrDefault(ds, Collections.emptyList());
  }

  public Set<Resource> getStartDatasets() {
    return Sets.difference(datasetConsumers.keySet(), datasetProducers.keySet());
  }

  public void setSubGraphId(Resource ds, int id) {
    subGraphIds.put(ds, id);
  }

  public int getSubGraphId(Resource ds) {
    return subGraphIds.getOrDefault(ds, -1);
  }

  public boolean isVisited(Resource op) {
    return visitedOperators.contains(op);
  }

  public void visit(Resource op) {
    visitedOperators.add(op);
  }

  public boolean isHub(Resource op) {
    return operatorInputs.get(op).size() > 1 || operatorOutputs.get(op).size() > 1;
  }

}
