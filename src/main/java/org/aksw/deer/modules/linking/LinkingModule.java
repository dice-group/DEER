package org.aksw.deer.modules.linking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.aksw.deer.helper.vocabularies.SPECS;
import org.aksw.deer.json.ParameterType;
import org.aksw.deer.modules.DeerModule;
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

/**
 * @author mofeed
 */
public class LinkingModule implements DeerModule {

  private static final Logger logger = Logger.getLogger(LinkingModule.class.getName());

  private static final String LINKS_PART = "linkspart";
  private static final String SPEC_FILE = "specfile";

  private static final String LINKS_PART_DESC = "Represents the position of the URI to be enriched in the links file";
  private static final String SPEC_FILE_DESC = "The specification file used for linking process";

  private String specFilePath;
  private String linksFilePath;
  private String linksPart;

  /**
   * @param model: the model of the dataset to be enriched
   * @param parameters: list of parameters needed for the processing include:
   * datasetFilePath,specFilePath,linksFilePath,linksPart
   * @return model enriched with links generated from a linking tool
   */

  public Model process(Model model, Map<String, String> parameters) {
    logger.info("--------------- Linking Module ---------------");

    //@todo: Where does data come from?

    for (String key : parameters.keySet()) {
      if (key.equalsIgnoreCase(SPEC_FILE)) {
        specFilePath = parameters.get(SPEC_FILE);
      } else if (key.equalsIgnoreCase(LINKS_PART)) {
        linksPart = parameters.get(LINKS_PART);
      } else {
        logger.error(
          "Invalid parameter key: " + key + ", allowed parameters for the linking module are: "
            + getParameters());
        logger.error("Exit GeoLift");
        System.exit(1);
      }
    }

    model = setPrefixes(model);
    Configuration cfg = new XMLConfigurationReader(specFilePath).read();
    model = addLinksToModel(model, cfg, linksPart);
    return model;
  }

  public List<String> getParameters() {
    List<String> parameters = new ArrayList<String>();
//		parameters.add("datasetSource");
    parameters.add(SPEC_FILE);
    parameters.add(LINKS_PART);
    return parameters;
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
   */
  @Override
  public List<String> getNecessaryParameters() {
    List<String> parameters = new ArrayList<String>();
    return parameters;
  }

  /**
   * @param model: the model of the dataset to be enriched
   * @param linksPart: represents the position of the URI to be enriched in the links file
   * @return model enriched with links generated from a linking tool
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
//		// TODO Auto-generated method stub
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
//					linksPath = args[i+1].substring(0,args[i+1].lastIndexOf("/"))+"/accept.nt";
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

  /* (non-Javadoc)
   * @see org.aksw.geolift.modules.GeoLiftModule#selfConfig(org.apache.jena.rdf.model.Model, org.apache.jena.rdf.model.Model)
   */
  @Override
  public Map<String, String> selfConfig(Model source, Model target) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<ParameterType> getParameterWithTypes() {
    List<ParameterType> parameters = new ArrayList<ParameterType>();
    parameters.add(new ParameterType(ParameterType.STRING, SPEC_FILE, SPEC_FILE_DESC, true));
    parameters.add(new ParameterType(ParameterType.STRING, LINKS_PART, LINKS_PART_DESC, true));

    return parameters;
  }

  @Override
  public Resource getType() {
    return SPECS.LinkingModule;
  }
}
