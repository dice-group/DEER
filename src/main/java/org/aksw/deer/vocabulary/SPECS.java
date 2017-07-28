package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SPECS {

  public static final String uri = "http://geoknow.org/specsontology/";
  public static final String prefix = ":";
  public static final Property hasUri = property("hasUri");
  public static final Property inputFile = property("inputFile");
  public static final Property outputFile = property("outputFile");
  public static final Property outputFormat = property("outputFormat");
  public static final Property fromEndPoint = property("fromEndPoint");
  public static final Property fromGraph = property("fromGraph");
  public static final Property graphTriplePattern = property("graphTriplePattern");
  public static final Property hasParameter = property("hasParameter");
  public static final Property hasKey = property("hasKey");
  public static final Property hasValue = property("hasValue");
  public static final Property hasModule = property("hasModule");
  public static final Property hasOperator = property("hasOperator");
  public static final Property hasInput = property("hasInput");
  public static final Property hasOutput = property("hasOutput");
  public static final Resource Dataset = resource("Dataset");
  public static final Resource Module = resource("Module");


  public static final Resource DereferencingModule = resource("DereferencingModule");
  public static final Resource LinkingModule = resource("LinkingModule");
  public static final Resource NLPModule = resource("NLPModule");
  public static final Resource FilterModule = resource("FilterModule");
  public static final Resource AuthorityConformationModule =
    resource("AuthorityConformationModule");
  public static final Resource PredicateConformationModule =
    resource("PredicateConformationModule");
  public static final Resource Operator = resource("Operator");

  public static final Resource CloneOperator = resource("CloneOperator");
  public static final Resource MergeOperator = resource("MergeOperator");
  public static final Resource ModuleParameter = resource("ModuleParameter");
  public static final Resource OperatorParameter = resource("OperatorParameter");
  public static final Resource FusionOperator = resource("FusionOperator");

  public static Property property(String name) {
    return ResourceFactory.createProperty(uri + name);
  }

  public static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

  public static String getURI() {
    return uri;
  }

}
