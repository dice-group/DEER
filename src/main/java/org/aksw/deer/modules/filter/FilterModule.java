/**
 *
 */
package org.aksw.deer.modules.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.deer.helper.vocabularies.SPECS;
import org.aksw.deer.json.ParameterType;
import org.aksw.deer.modules.DeerModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
public class FilterModule implements DeerModule {

  public static final String TRIPLES_PATTERN = "triplesPattern";
  public static final String TRIPLES_PATTERN_DESC =
    "Set of triple pattern to run against the input model of the filter module. " +
      "By default, this parameter is set to ?s ?p ?o. which generates the whole " +
      "input model as output, changing the values of " +
      "?s, ?p and/or ?o will restrict the output model";
  private static final Logger logger = Logger.getLogger(FilterModule.class.getName());
  //	private static final int MAX_TRIPLE_PATTERN_SIZE = 50;
  private Model model = ModelFactory.createDefaultModel();

  // parameters list
  private String triplesPattern = "?s ?p ?o .";
//	private Set<String> blackList = null;


  /**
   * @author sherif
   */
  public FilterModule(Model m) {
    super();
    model = m;
  }

  /**
   * @author sherif
   */
  public FilterModule() {
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#process(org.apache.jena.rdf.model.Model, java.util.Map)
   */
  @Override
  public Model process(Model model, Map<String, String> parameters) {
    logger.info("--------------- Filter Module ---------------");
    this.model = this.model.union(model);
    if (parameters.containsKey(TRIPLES_PATTERN)) {
      triplesPattern = parameters.get(TRIPLES_PATTERN);
    }
    Model filteredModel = filterModel();
    return filteredModel;
  }

  public Model filterModel() {
    Model resultModel = ModelFactory.createDefaultModel();
    List<Property> accepted = new ArrayList<Property>();
    if (triplesPattern.contains(" ")) {
      for (String str : triplesPattern.split(" ")) {
        accepted.add(ResourceFactory.createProperty(str));
      }
    } else { // if only one property
      accepted.add(ResourceFactory.createProperty(triplesPattern));
    }
    StmtIterator listStatements = model.listStatements();
    while (listStatements.hasNext()) {
      Statement stat = listStatements.next();
      if (accepted.contains(stat.getPredicate())) {
        resultModel.add(stat);
      }
    }
    return resultModel;

    //		// IMPLEMENTATION 2
    //		Model resultModel = ModelFactory.createDefaultModel();
    //		Model accepted = ModelFactory.createDefaultModel();
    //		for(String str : triplesPattern.split(", *<")){
    //			Resource s = ResourceFactory.createResource(str.split(">")[0].replaceAll("<", "").replaceAll(">", "").trim());
    ////			System.out.println(str.split(">")[1].replaceAll("<", "").replaceAll(">", "").trim());
    //			Property p = ResourceFactory.createProperty(str.split(" ")[1].replaceAll("<", "").replaceAll(">", ""));
    //			if(str.split(">")[2].trim().startsWith("<")){
    //				Resource o = ResourceFactory.createResource(str.split(">")[2].replaceAll("<", "").replaceAll(">", "").trim());
    //				accepted.add(s, p, o);
    //			}else if(str.split(">")[2].trim().startsWith("\"")){
    //				org.apache.jena.rdf.model.Literal o = ResourceFactory.createPlainLiteral(str.split(">")[2]);
    //				accepted.add(s, p, o);
    //			}
    //		}
    //		resultModel = accepted.intersection(model);
    //		return resultModel;

    // IMPLEMENTATION 1
    //		String sparqlQueryString = 	"CONSTRUCT {?s ?p ?o} " +
    //									"WHERE {" + triplesPattern +"} " ;
    //		QueryFactory.create(sparqlQueryString);
    //		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, model);
    //		resultModel = qexec.execConstruct();

    //		Model resultModel = ModelFactory.createDefaultModel();
    //		String sparqlQueryString = 	"CONSTRUCT {?s ?p ?o} " +
    //									"WHERE {" + triplesPattern +"} " ;
    //		QueryFactory.create(sparqlQueryString);
    //		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, model);
    //		resultModel = qexec.execConstruct();
  }


  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
   */
  @Override
  public List<String> getParameters() {
    List<String> parameters = new ArrayList<String>();
    parameters.add(TRIPLES_PATTERN);
    return parameters;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
   */
  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<String>();
    parameters.add(TRIPLES_PATTERN);
    return parameters;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#selfConfig(org.apache.jena.rdf.model.Model, org.apache.jena.rdf.model.Model)
   */
  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    Map<String, String> parameters = new HashMap<String, String>();
    Model intersection = source.intersection(target);
    if (intersection.isEmpty()) {
      return null;
    }
    triplesPattern = new String();
    StmtIterator listStatements = intersection.listStatements();
    while (listStatements.hasNext()) {
      Statement stmnt = listStatements.next();
      triplesPattern += stmnt.getPredicate() + " ";
    }
    // IMPLEMENTATION 2
    //		Map<String, String> parameters = new HashMap<String, String>();
    //		Model intersection = source.intersection(target);
    //		if(intersection.isEmpty()){
    //			return null;
    //		}
    //		triplesPattern = new String();
    //		StmtIterator listStatements = intersection.listStatements();
    //		while(listStatements.hasNext()){
    //			Statement stmnt = listStatements.next();
    //			String s, p, o;
    //			s = "<" + stmnt.getSubject() + ">" ;
    //			p = "<" + stmnt.getPredicate() + ">";
    //			if(stmnt.getObject().isLiteral()){
    //				o = "\"" + stmnt.getObject() + "\"";
    //			}else if(stmnt.getObject().isAnon()){
    //				o = "?o";
    //			}else{
    //				o = "<" + stmnt.getObject() + ">" ;
    //			}
    //			triplesPattern += s + " " + p + " " + o + "," ;
    //		}

    // IMPLEMENTATION 1
    //		Model intersection = source.intersection(target);
    //		triplesPattern += "{ ";
    //		StmtIterator listStatements = intersection.listStatements();
    //		while(listStatements.hasNext()){
    //			Statement stmnt = listStatements.next();
    //			String s, p, o;
    ////			s = "<" + stmnt.getSubject() + "> <" + stmnt.getPredicate() + ">" ;
    //			s = "?s";
    //			p = "<" + stmnt.getPredicate() + ">";
    //			if(stmnt.getObject().isLiteral()){
    //				o = "\"" + stmnt.getObject() + "\"";
    //			}else if(stmnt.getObject().isAnon()){
    //				o = "?o";
    //			}else{
    //				o = "<" + stmnt.getObject() + ">" ;
    //			}
    //			triplesPattern += "{" + s + " " + p + " " + o + " . }" +
    //					((listStatements.hasNext())? "UNION" : "");
    //		}
    //		triplesPattern += " }";
    //		Set<Property> ps = new HashSet<Property>();
    //		long i = 0;
    //		if(ps.size()> MAX_TRIPLE_PATTERN_SIZE){
    //			return null;
    //		}
    //		for(Property p : ps){
    //			triplesPattern += "{?s <" + p.toString() + ">  ?o" + i + " . }";
    //			i++;
    //			triplesPattern += (i < ps.size())? "UNION" : "";
    //		}
    //		triplesPattern += "}";
    //		triplesPattern += "{";
    //		Set<Property> ps = new HashSet<Property>();
    //		StmtIterator listStatements = target.listStatements();
    //		while(listStatements.hasNext()){
    //			Statement s = listStatements.next();
    //			ps.add(s.getPredicate());
    //		}
    //		long i = 0;
    //		if(ps.size()> MAX_TRIPLE_PATTERN_SIZE){
    //			return null;
    //		}
    //		for(Property p : ps){
    //			triplesPattern += "{?s <" + p.toString() + ">  ?o" + i + " . }";
    //			i++;
    //			triplesPattern += (i < ps.size())? "UNION" : "";
    //		}
    //		triplesPattern += "}";
    parameters.put(TRIPLES_PATTERN, triplesPattern);
    return parameters;
  }

  @Override
  public List<ParameterType> getParameterWithTypes() {
    List<ParameterType> parameters = new ArrayList<ParameterType>();
    parameters
      .add(new ParameterType(ParameterType.STRING, TRIPLES_PATTERN, TRIPLES_PATTERN_DESC, true));

    return parameters;
  }

  @Override
  public Resource getType() {
    return SPECS.FilterModule;
  }

}
