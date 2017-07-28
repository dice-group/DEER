package org.aksw.deer.enrichment.linking;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.vocabulary.SPECS;
import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.reader.xml.XMLConfigurationReader;
import org.aksw.limes.core.io.mapping.AMapping;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.log4j.Logger;
import ro.fortsoft.pf4j.Extension;

/**
 * @author mofeed
 */
@Extension
public class LinkingEnrichmentOperator extends AEnrichmentOperator {

  private static final Logger logger = Logger.getLogger(LinkingEnrichmentOperator.class.getName());

  private static final String LINKS_PART = "linkspart";
  private static final String SPEC_FILE = "specfile";

  private static final String LINKS_PART_DESC = "Represents the position of the URI to be enriched in the links file";
  private static final String SPEC_FILE_DESC = "The specification file used for org.aksw.deer.resources.linking process";

  private String specFilePath;
  private String linksFilePath;
  private String linksPart;

  public LinkingEnrichmentOperator() {
    super();
  }

  /**
   * @return model enriched with links generated from a org.aksw.deer.resources.linking tool
   */

  protected List<Model> process() {
    //@todo: Where does data come from?
    //@todo: implement ability to link internal datasets
    for (String key : parameters.keySet()) {
      if (key.equalsIgnoreCase(SPEC_FILE)) {
        specFilePath = parameters.get(SPEC_FILE);
      } else if (key.equalsIgnoreCase(LINKS_PART)) {
        linksPart = parameters.get(LINKS_PART);
      } else {
        logger.error(
          "Invalid parameter key: " + key + ", allowed parameters for the org.aksw.deer.resources.linking enrichment are: "
            + getParameters());
        logger.error("Exit GeoLift");
        System.exit(1);
      }
    }

    Model model = setPrefixes(models.get(0));
    Configuration cfg = new XMLConfigurationReader(specFilePath).read();
    model = addLinksToModel(model, cfg, linksPart);
    return Lists.newArrayList(model);
  }

  public List<String> getParameters() {
    List<String> parameters = new ArrayList<String>();
//		parameters.add("datasetSource");
    parameters.add(SPEC_FILE);
    parameters.add(LINKS_PART);
    return parameters;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.enrichment.GeoLiftModule#getNecessaryParameters()
   */
  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<String>();
    return parameters;
  }


  @Override
  public String getDescription() {
    return null;
  }

  /**
   * @param model: the model of the dataset to be enriched
   * @param linksPart: represents the position of the URI to be enriched in the links file
   * @return model enriched with links generated from a org.aksw.deer.resources.linking tool
   */
  private Model addLinksToModel(Model model, Configuration cfg, String linksPart) {
    AMapping mapping = Controller.getMapping(cfg).getAcceptanceMapping();
    Property predicate = ResourceFactory.createProperty(cfg.getAcceptanceRelation());

    for (String s : mapping.getMap().keySet()) {
      Resource subject = ResourceFactory.createResource(s);
      for (String t : mapping.getMap().get(s).keySet()) {
        Resource object = ResourceFactory.createResource(t);
        Statement stmt;
        if (linksPart.equals("source")) {
          stmt = ResourceFactory.createStatement(subject, predicate, object);
        } else {
          stmt = ResourceFactory.createStatement(object, predicate, subject);
        }
        model.add(stmt);
      }
    }

    return model;
  }

  /**
   * @return model with prefixes added This function adds prefixes required
   */
  private Model setPrefixes(Model model) {
    String gn = "http://www.geonames.org/ontology#";
    String owl = "http://www.w3.org/2002/07/owl#";

    model.setNsPrefix("gn", gn);
    model.setNsPrefix("owl", owl);
    return model;
  }
//	////////////////////////////////////////////////////////////////////////////////////////////////
//	public static void main(String[] args) {
//		Map<String, String> parameters=new HashMap<String, String>();
//		String linksPath="";
//		if(args.length > 0)
//		{
//			for(int i=0;i<args.length;i+=2)
//			{
//				if(args[i].equals("-d"))
//					parameters.put("datasetSource",args[i+1]);
//				if(args[i].equals("-s"))
//				{
//					parameters.put(SPEC_FILE,args[i+1]);
//					linksPath = args[i+1].substring(0,args[i+1].lastIndexOf("/"))+"/setCallback.nt";
//					parameters.put(LINKS_FILE,linksPath);
//				}
//				if(args[i].equals("-p"))
//					parameters.put(LINKS_PART,args[i+1]);
//
//			}
//		}
//		try {
//			Model model=org.aksw.deer.io.Reader.readModel(parameters.get("datasetSource"));
//			LinkingModule l= new LinkingModule();
//			model=l.process(model, parameters);
//			try{
//
//				File file = new File(linksPath);
//
//				file.delete();
//
//			}catch(Exception e){
//
//				e.printStackTrace();
//
//			}
//			model.write(System.out,"TTL");
//
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
//		System.out.println("Finished");
//	}

  @Override
  public ArityBounds getArityBounds() {
    return new ArityBoundsImpl(1, 2, 1, 1);
  }

  /* (non-Javadoc)
     * @see org.aksw.geolift.enrichment.GeoLiftModule#selfConfig(org.apache.jena.rdf.model.Model, org.apache.jena.rdf.model.Model)
     */
  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    return null;
  }

}
