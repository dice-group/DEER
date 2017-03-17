/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

//import java.util.HashMap;
//import java.util.Map;
//
//import org.aksw.deer.helper.vacabularies.SPECS;
//import org.aksw.deer.io.Reader;
//import org.apache.log4j.Logger;
//
//import com.google.common.collect.HashMultimap;
//import com.google.common.collect.Multimap;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.RDFNode;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.rdf.model.Statement;
//import org.apache.jena.rdf.model.StmtIterator;
//import org.apache.jena.vocabulary.RDFS;

/**
 * @author sherif
 * 
 * replaced by RDFConfigExecuter
 *
 */


public class RDFConfigReader {
//	private static final Logger logger = Logger.getLogger(RDFConfigReader.class.getName());
//	public static 	Model configModel;
//	public static Multimap<String, Map<String, String>> parameters = HashMultimap.create();
//
//	
//	public static Multimap<String, Map<String, String>> getParameters(String inputFile){
//		configModel =  Reader.readModel(inputFile);
//		configModel.write(System.out,"TTL");
//		// Start by first step
//		RDFNode step = getFirstStep();
//		// loop through all steps
//		while(true){
//			Map<RDFNode, Map<RDFNode, RDFNode>> module2ModuleParameters = getModule2ModuleParameters(step);
//			int moduleId = 1;
//			for(RDFNode module : module2ModuleParameters.keySet()){
//				for(RDFNode moduleKey : module2ModuleParameters.get(module).keySet()){
//					Map<String, String> param = new HashMap<String, String>();
//					param.put(moduleKey.toString(), module2ModuleParameters.get(module).get(moduleKey).toString());
//					parameters.put(moduleId++ + "_" + getModuleName(module), param);
//				}
//			}
//			if(isLastStep(step)){
//				break;
//			}
//			step = getNextStep(step);
//			if(step == null){ // no next step
//				break;
//			}
//		}
//		return parameters;
//	}
//
//	/**
//	 * @param module
//	 * @return
//	 * @author sherif
//	 */
//	private static String getModuleName(RDFNode module) {
//		StmtIterator itr = configModel.listStatements((Resource) module, RDFS.label, (RDFNode) null);
//		if(itr.hasNext()){
//			return itr.next().getObject().toString().toLowerCase(); 
//		}
//		logger.error("Module " + module + " have no " + RDFS.label + ", exit with error.");
////		System.exit(1);
//		return null;
//	}
//
//	/**
//	 * 
//	 * @author sherif
//	 */
//	private static Map<RDFNode, Map<RDFNode, RDFNode>> getModule2ModuleParameters(RDFNode step) {
//		Map<RDFNode, Map<RDFNode, RDFNode>> module2ModuleParameters = new HashMap<RDFNode, Map<RDFNode,RDFNode>>();
//		System.out.println(step); // do work
//		// read module/operator name
//		RDFNode module = getModule(step);
//		Map<RDFNode, RDFNode> moduleParameters = getModuleParameters(module);
//		System.out.println(module);
//		System.out.println(moduleParameters);
//		module2ModuleParameters.put(module, moduleParameters);
//		//TODO handle operators
//		return module2ModuleParameters;
//
//	}
//	
//	/**
//	 * @param module
//	 * @return the input module parameters'
//	 * @author sherif
//	 */
//	private static Map<RDFNode, RDFNode> getModuleParameters(RDFNode module) {
//		RDFNode parameter = null;
//		RDFNode key = null;
//		RDFNode value = null;
//		Map<RDFNode, RDFNode> moduleParameters = new HashMap<RDFNode, RDFNode>();
//		StmtIterator stItr = configModel.listStatements((Resource) module, SPECS.hasParameter, (RDFNode) null);
//		while(stItr.hasNext()){
//			parameter =  stItr.next().getObject(); 
//			StmtIterator keyItr = configModel.listStatements((Resource) parameter, SPECS.hasKey, (RDFNode) null);
//			if(keyItr.hasNext()){
//				key =  keyItr.next().getObject(); 
//			}
//			StmtIterator valueItr = configModel.listStatements((Resource) parameter, SPECS.hasValue, (RDFNode) null);
//			if(valueItr.hasNext()){
//				value =  valueItr.next().getObject(); 
//			}
//			moduleParameters.put(key, value);
//		}
//		return moduleParameters;
//	}
//
//	private static RDFNode getModule(RDFNode step){
//		StmtIterator stItr = configModel.listStatements((Resource) step, SPECS.hasModule, (RDFNode) null);
//		if(stItr.hasNext()){
//			return stItr.next().getObject(); 
//		}
//		return null;
//	}
//
//	/**
//	 * @param step
//	 * @return Next step if any, otherwise a null
//	 * @author sherif
//	 */
//	private static RDFNode getNextStep(RDFNode step){
//		StmtIterator stItr = configModel.listStatements((Resource) step, SPECS.nextStep, (RDFNode) null);
//		if(stItr.hasNext()){
//			return (Resource) stItr.next().getObject(); 
//		}
//		return null;
//	}
//
//	/**
//	 * @return
//	 * @author sherif
//	 */
//	private static boolean isLastStep(RDFNode step) {
//		StmtIterator s = configModel.listStatements((Resource) step, SPECS.isLastStep, (RDFNode) null);
//		if(s.hasNext()){
//			Statement stat = s.next();
//			RDFNode object = stat.getObject(); 
//			if(object.toString().startsWith("true")){ 
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * @return first step from specs file
//	 * @author sherif
//	 */
//	private static RDFNode getFirstStep(){
//		RDFNode firstStep = null;
//		StmtIterator s = configModel.listStatements(null, SPECS.isFirstStep, (RDFNode) null);
//		Statement stat = s.next();
//		RDFNode subject = stat.getSubject();
//		RDFNode object = stat.getObject(); 
//		if(object.toString().startsWith("true")){ 
//			firstStep = subject;
//		}
//		while(s.hasNext()){
//			object = stat.getObject(); 
//			subject = stat.getSubject();
//			if(object.toString().startsWith("true")){ 
//				logger.error("RDF Specs file error, more then one start step, exit with error.");
//				System.exit(1);
//			}
//		}
//		return firstStep;
//	}
//	
//	public static void main(String args[]){
//		System.out.println(getParameters(args[0]));
//	}
}
