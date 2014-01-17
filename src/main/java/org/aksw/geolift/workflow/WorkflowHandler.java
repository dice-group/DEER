/**
 * 
 */
package org.aksw.geolift.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.modules.Dereferencing.URIDereferencing;
import org.aksw.geolift.modules.linking.Linking;
import org.aksw.geolift.modules.nlp.NlpGeoEnricher;
import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * @author sherif
 *
 */
public class WorkflowHandler{
	private static final Logger logger = Logger.getLogger(GeoLiftModule.class.getName());

	private Model inputModel = ModelFactory.createDefaultModel();


	/**
	 * @return the inputModel
	 */
	public Model getEnrichedModel() {
		return inputModel;
	}
	/**
	 * @param inputModel the inputModel to set
	 */
	public void setInputModel(Model inputModel) {
		this.inputModel = inputModel;
	}


	/**
	 * @param moduleName: the name of the module to be executed  
	 * @param inputModel: input data model for the module
	 * @param modueParameters : the specific module parameters 
	 * @return Geo-spatial enriched model containing in addition to the original model the new discovered 
	 * 			information through the usage of different module
	 * @author sherif
	 */
	Model executeModule(String moduleName, Model inputModel, Map<String, String> modueParameters){
		Model enrichedModel = ModelFactory.createDefaultModel();

		if(moduleName.toLowerCase().equals("nlp")){
			NlpGeoEnricher geoEnricher= new NlpGeoEnricher();
			enrichedModel = geoEnricher.process(inputModel, modueParameters);
			return enrichedModel;
		}
		if(moduleName.toLowerCase().equals("linking")){
			Linking geoEnricher= new Linking();
			enrichedModel = geoEnricher.process(inputModel, modueParameters);
			return enrichedModel;
		}
		if(moduleName.toLowerCase().equals("dereferencing")){
			URIDereferencing geoEnricher= new URIDereferencing();
			enrichedModel = geoEnricher.process(inputModel, modueParameters);
			return enrichedModel;
		}
		logger.error(moduleName + " module is not yet implemented,\n" +
				"Currently,the nlp, linking and dereferencing) modules are implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}

	/**
	 * @param startModel: A Model contains the dataset   
	 * @param parameters: Multimap of each Module name and its parameters
	 * @throws IOException
	 *@author sherif
	 */
	public WorkflowHandler(Model startModel, Multimap<String, Map<String, String>> parameters) throws IOException {	
		inputModel =  startModel;
		SortedSet<String> modules = new TreeSet<String>(parameters.keySet());
		
		int count =1;
		for(String key: modules)
		{
			String moduleName = key.substring(key.indexOf("_")+1);

			Collection<Map<String, String>> moduleParameters = parameters.get(key);
			Iterator itr = moduleParameters.iterator();
			Map<String, String> param = new HashMap<String, String>();
			while(itr.hasNext()){
				param.putAll((Map<String, String>) itr.next());
			}		
			logger.info("----------------------------------------------------------------------------------------------------------------");
			logger.info("("+ count++ + ") Runing module: " + moduleName.toUpperCase() + " with parameters: " + param);
			
			inputModel =  executeModule(moduleName, inputModel, param);
		}
	}

	public static void main(String args[]) throws IOException{
		Model startModel =  Reader.readModel(args[0]);
		Multimap<String, Map<String, String>> parameters = TSVConfigReader.getParameters(args[1]);
		WorkflowHandler wfh = new WorkflowHandler(startModel, parameters);
		wfh.getEnrichedModel().write(System.out, "TTL");
	}
}













