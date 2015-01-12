package org.aksw.deer.modules.linking;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.json.ParameterType;
import org.aksw.deer.modules.DeerModule;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.uni_leipzig.simba.controller.PPJoinController;

/**
 * @author mofeed
 *
 */
public class LinkingModule implements DeerModule{
	private static final Logger logger = Logger.getLogger(LinkingModule.class.getName());
	
	private static final String LINKS_PART = "linkspart";
	private static final String LINKS_FILE = "linksfile";
	private static final String SPEC_FILE = "specfile";
	
	private static final String LINKS_PART_DESC = "Represents the position of the URI to be enriched in the links file";
	private static final String LINKS_FILE_DESC = "file to save links resulted from the linking process";
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

	public Model process(Model model, Map<String, String> parameters) 
	{
		logger.info("--------------- Linking Module ---------------");
		//Copy parameters from Map into List
//		String datasetSource = parameters.get("datasetSource"); 
		
		for(String key : parameters.keySet()){
			if(key.equalsIgnoreCase(SPEC_FILE)){
				specFilePath = parameters.get(SPEC_FILE);
			}else if(key.equalsIgnoreCase(LINKS_FILE)){
				linksFilePath = parameters.get(LINKS_FILE);
			}else if(key.equalsIgnoreCase(LINKS_PART)){
				linksPart = parameters.get(LINKS_PART);
			}else{
				logger.error("Invalid parameter key: " + key + ", allowed parameters for the linking module are: " + getParameters());
				logger.error("Exit GeoLift");
				System.exit(1);
			}
		}
	
		model = setPrefixes(model);
		linkingProcess(specFilePath);
		model= addLinksToModel(model, linksFilePath,linksPart);
		return model;
	}

	public List<String> getParameters() 
	{
		List<String> parameters = new ArrayList<String>();
//		parameters.add("datasetSource");
		parameters.add(SPEC_FILE);
		parameters.add(LINKS_FILE);
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
	 * @param specFilePath: the spec/xml file path for the linking process
	 * The result is in a file accept.nt
	 */
	private void linkingProcess(String specFilePath)
	{
		PPJoinController controller = new PPJoinController();
		controller.run(specFilePath); 
	}
	/**
	 * @param model: the model of the dataset to be enriched
	 * @param linksFilePath: the links file path
	 * @param linksPart: represents the position of the URI to be enriched in the links file
	 * @return model enriched with links generated from a linking tool
	 */
	private Model addLinksToModel(Model model,String linksFilePath, String linksPart)
	{
		Model linksModel = getLinks(linksFilePath);
		System.out.println(linksFilePath);
		linksModel = setPrefixes(linksModel);
		StmtIterator iter = linksModel.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) 
		{
			Statement stmt      = iter.nextStatement();  // get next statement
			Resource  subject   = stmt.getSubject();     // get the subject
			Property  predicate = model.createProperty(stmt.getPredicate().toString()) ;//model.createProperty(model.expandPrefix(stmt.getPredicate().toString())) ;// get the predicate
			RDFNode   object    = stmt.getObject();      // get the object
			Resource resource;
			//model.expandPrefix(predicate.toString())
			if(linksPart.equals("source"))
			{
				resource = model.getResource(subject.toString());
				resource.addProperty(predicate, object);
			}
			else
			{
				resource = model.getResource(object.toString());
				resource.addProperty(predicate, subject);
			}
		} 
		return model; 
	}

	/**
	 * @param linksFilePath
	 * @return a model representing the whole linking tuples generated from a linking tool
	 * This function reads the links generated by a linking tool into a model
	 */
	private Model getLinks(String linksFilePath)
	{ 	
		Model model= ModelFactory.createDefaultModel();
		model=org.aksw.deer.io.Reader.readModel(linksFilePath);
		return model;
	}

	/**
	 * @param link : represents link between source and target datasets
	 * @return Map representing the three parts of the link source,relation, and target
	 * This function separates each link into its three parts source,relation,target and they are encapsulated in HashMap
	 */
	private Map<String, String> linkInfoExtraction(String link)
	{
		Map<String, String> linkInfo= null;
		String[] splittedLink = link.split("\\s+");
		if(splittedLink.length > 3)
		{
			linkInfo= new HashMap<String, String>();
			String linkInfoPart= splittedLink[0].substring(1,splittedLink[0].length()-1);
			linkInfo.put("sourceURI", linkInfoPart);
			linkInfoPart= splittedLink[1].substring(1,splittedLink[1].length()-1);
			linkInfo.put("relation", linkInfoPart);
			linkInfoPart= splittedLink[2].substring(1,splittedLink[2].length()-1);
			linkInfo.put("targetURI", linkInfoPart);
		}
		return linkInfo;
	}

	/**
	 * @param model
	 * @return model with prefixes added
	 * This function adds prefixes required
	 */
	private Model setPrefixes(Model model)
	{
		String gn = "http://www.geonames.org/ontology#";
		String owl = "http://www.w3.org/2002/07/owl#";


		model.setNsPrefix( "gn", gn );
		model.setNsPrefix( "owl", owl );
		return model;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> parameters=new HashMap<String, String>();
		String linksPath="";
		if(args.length > 0)
		{
			for(int i=0;i<args.length;i+=2)
			{
				if(args[i].equals("-d"))
					parameters.put("datasetSource",args[i+1]);
				if(args[i].equals("-s"))
				{
					parameters.put(SPEC_FILE,args[i+1]);
					linksPath = args[i+1].substring(0,args[i+1].lastIndexOf("/"))+"/accept.nt";
					parameters.put(LINKS_FILE,linksPath);
				}
				if(args[i].equals("-p"))
					parameters.put(LINKS_PART,args[i+1]);

			}
		}
		try {
			Model model=org.aksw.deer.io.Reader.readModel(parameters.get("datasetSource"));
			LinkingModule l= new LinkingModule();
			model=l.process(model, parameters);
			try{

				File file = new File(linksPath);

				file.delete();

			}catch(Exception e){

				e.printStackTrace();

			}
			model.write(System.out,"TTL");

		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Finished");
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#selfConfig(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.rdf.model.Model)
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
        parameters.add(new ParameterType(ParameterType.STRING, LINKS_FILE, LINKS_FILE_DESC, true));
        parameters.add(new ParameterType(ParameterType.STRING, LINKS_PART, LINKS_PART_DESC, true));

        return parameters;
    }
    
    @Override
	public Resource getType(){
		return SPECS.LinkingModule;
	}
}
