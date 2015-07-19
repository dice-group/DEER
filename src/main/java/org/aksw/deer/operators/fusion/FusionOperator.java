/**
 * 
 */
package org.aksw.deer.operators.fusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.operators.DeerOperator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author sherif
 *
 */
public class FusionOperator implements DeerOperator {
	private static final Logger logger = Logger.getLogger(FusionOperator.class.getName());
	public static final String FUNCTIONAL_PROPERTY ="functionalproperty";
	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.operators.ModelOperator#run(java.util.List)
	 */
	@Override
	public List<Model> process(final List<Model> models,final Map<String, String> parameters) {
		logger.info("--------------- Fusion Operator ---------------");
		Set<Property> funcProp = new HashSet<>();
		if(parameters != null ){
			for(String key : parameters.keySet()){
				if(key.toLowerCase().startsWith(FUNCTIONAL_PROPERTY)){
					funcProp.add(ResourceFactory.createProperty(parameters.get(key)));
				}
			}
		}
		List<Model> result = new ArrayList<Model>();
		for (Property fp : funcProp) {
			for(Model m : models){
				Model pModel = getAssociatedTriples(m, fp);
				pModel = addAdditionalTriples(pModel);
			}
			

		}
		return result;
	}
	
	
	/**
	 * @param pModel
	 * @return
	 * @author sherif
	 */
	private Model addAdditionalTriples(Model pModel) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @param m
	 * @param fp
	 * @return
	 * @author sherif
	 */
	private Model getAssociatedTriples(Model m, Property fp) {
		Model result = ModelFactory.createDefaultModel();
		StmtIterator listStatements = m.listStatements(null, fp, (RDFNode) null);
		while(listStatements.hasNext()) {
			Statement s = listStatements.next();
			result.createReifiedStatement(s);
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("cloneCount");
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
        
}
