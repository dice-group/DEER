package org.aksw.deer.parameter;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.*;

import java.util.*;

/**
 * A {@code ParameterConversion} to convert between RDF lists and
 * instances of {@code List<Map<Property, RDFNode>>}
 * <p>
 * A {@code DictListParameterConversion} is constructed with the list of {@link Property} that
 * are should be extracted from the resources (typically blank nodes) in a RDF list.
 */
public class DictListParameterConversion implements ParameterConversion {

  private static final short RESOURCE = 1;
  private static final short LITERAL = 2;

  /**
   * List of {@code Property} to be extracted
   */
  private Set<Property> properties = new HashSet<>();
  /**
   * Force mode of this {@code DictListParameterConversion}
   */
  private short force = 0;

  /**
   * Constructor
   * @param properties  varargs of {@code Property} to be extracted
   */
  public DictListParameterConversion(Property...properties) {
    this.properties.addAll(Lists.newArrayList(properties));
  }

  @Override
  @SuppressWarnings("unchecked")
  public RDFNode toRDF(Object object) {
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
  public Object fromRDF(RDFNode node) {
    List<Map<Property, RDFNode>> dictList = new ArrayList<>();
    node.as(RDFList.class).iterator().forEachRemaining(n -> {
      Resource r = n.asResource();
      Map<Property, RDFNode> nodeMap = new HashMap<>();
      properties.forEach(p -> {
        RDFNode pValue = null;
        if (r.hasProperty(p)) {
          pValue = r.getProperty(p).getObject();
          switch (force) {
            case RESOURCE:
              pValue = pValue.asResource();
              break;
            case LITERAL:
              pValue = pValue.asLiteral();
              break;
          }
        }
          nodeMap.put(p, pValue);
      });
      dictList.add(nodeMap);
    });
    return dictList;
  }

  /**
   * Force this {@code DictListParameterConversion} to always convert
   * its values to instances of {@link Resource}, effectively throwing an exception if a
   * {@link Literal} is encountered.
   *
   * @return this {@code DictListParameterConversion}, for method chaining
   */
  public DictListParameterConversion forceResource() {
    this.force = RESOURCE;
    return this;
  }

  /**
   * Force this {@code DictListParameterConversion} to always convert
   * its values to instances of {@link Literal}, effectively throwing an exception if a
   * {@link Resource} is encountered.
   *
   * @return this {@code DictListParameterConversion}, for method chaining
   */
  public DictListParameterConversion forceLiteral() {
    this.force = LITERAL;
    return this;
  }
}
