package org.aksw.deer.parameter;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.*;

import java.util.*;

/**
 * @author Kevin Dreßler
 */

public class DictListParameterConversion implements ParameterConversion {

  private static final short RESOURCE = 1;
  private static final short LITERAL = 2;

  private List<Property> properties;
  private short force = 0;

  public DictListParameterConversion(Property...properties) {
    this.properties = Lists.newArrayList(properties);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RDFNode serialize(Object object) {
    List<Map<Property, RDFNode>> dictList = (List<Map<Property, RDFNode>>) object;
    Model model = ModelFactory.createDefaultModel();
    RDFList list = model.createList();
    dictList.forEach(dict -> {
      Resource bNode = model.createResource();
      properties.forEach(property -> bNode.addProperty(property, dict.get(property)));
      list.add(bNode);
    });
    return list;
  }

  @Override
  public Object deserialize(RDFNode node) {
    List<Map<Property, RDFNode>> dictList = new ArrayList<>();
    node.as(RDFList.class).iterator().forEachRemaining(n -> {
      Resource r = n.asResource();
      Map<Property, RDFNode> nodeMap = new HashMap<>();
      properties.forEach(p -> {
        RDFNode pValue = r.getProperty(p).getObject();
        switch (force) {
          case RESOURCE:
            pValue = pValue.asResource();
            break;
          case LITERAL:
            pValue = pValue.asLiteral();
            break;
        }
          nodeMap.put(p, pValue);
      });
      dictList.add(nodeMap);
    });
    return dictList;
  }

  public DictListParameterConversion forceResource() {
    this.force = RESOURCE;
    return this;
  }

  public DictListParameterConversion forceLiteral() {
    this.force = LITERAL;
    return this;
  }
}
