package org.aksw.geolift.modules.linking;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.uni_leipzig.simba.controller.PPJoinController;
import org.aksw.geolift.json.ParameterType;

/**
 * @author mofeed
 *
 */
public class Linking implements GeoLiftModule
{

	List<String> parametersList= new ArrayList<String>();
	/**
	 * @param model: the model of the dataset to be enriched
	 * @param parameters: list of parameters needed for the processing include:
	 * datasetFilePath,specFilePath,linksFilePath,linksPart
	 * @return model enriched with links generated from a linking tool
	 */

	public Model process(Model model, Map<String, String> parameters) 
	{
		// TODO Auto-generated method stub
		//Copy parameters from Map into List
		String datasetSource = parameters.get("datasetSource"); parametersList.add("datasetSource");
		String specFilePath = parameters.get("specFilePath"); parametersList.add("specFilePath");
		String linksFilePath = parameters.get("linksFilePath"); parametersList.add("linksFilePath");
		String linksPart=parameters.get("linksPart"); parametersList.add("linksPart");

		model = setPrefixes(model);
		linkingProcess(specFilePath);
		model= addLinksToModel(model, linksFilePath,linksPart);
		return model;
	}

	public List<String> getParameters() 
	{
		// TODO Auto-generated method stub
		if(parametersList.size()>0)
		{
			parametersList.add("input");
			return parametersList;
		}
		else
			return null;
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
		linksModel=setPrefixes(linksModel);
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
		model=org.aksw.geolift.io.Reader.readModel(linksFilePath);
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
					parameters.put("specFilePath",args[i+1]);
					linksPath = args[i+1].substring(0,args[i+1].lastIndexOf("/"))+"/accept.nt";
					parameters.put("linksFilePath",linksPath);
				}
				if(args[i].equals("-p"))
					parameters.put("linksPart",args[i+1]);

			}
		}
		try {
			Model model=org.aksw.geolift.io.Reader.readModel(parameters.get("datasetSource"));
			Linking l= new Linking();
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

    @Override
    public List<ParameterType> getParameterWithTypes() {
        List<ParameterType> parameters = new ArrayList<ParameterType>();
        parameters.add(new ParameterType(ParameterType.STRING, "specFilePath", "The path to specification file used for linking process", true));
        parameters.add(new ParameterType(ParameterType.STRING, "linksFilePath", "The path to links file resulted from the linking process", true));
        parameters.add(new ParameterType(ParameterType.STRING, "linksPart", "Represents the position of the URI to be enriched in the links file", true));

        return parameters;
    }
}
