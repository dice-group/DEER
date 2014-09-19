/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.Comparator;

import javax.swing.text.AsyncBoxView.ChildLocator;

import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.workflow.rdfspecs.SpecsOntology;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author sherif
 *
 */
public class RefinementNode implements Comparable<RefinementNode> {

	public GeoLiftModule module;
	public double fitness = Double.MAX_VALUE;
	public Model inputModel = ModelFactory.createDefaultModel();
	public Model outputModel = ModelFactory.createDefaultModel();
	public Model configModel = ModelFactory.createDefaultModel();
	public Resource inputDataset = ResourceFactory.createResource();
	public Resource outputDataset = ResourceFactory.createResource();
//	public int childNr;

	/**
	 * 
	 *@author sherif
	 */
	public RefinementNode() {
		super();
		configModel.setNsPrefix("gl", SpecsOntology.uri);
	}

	/**
	 * @param module
	 * @param fitness
	 * @param outputModel
	 * @param configModel
	 * @param childNr
	 *@author sherif
	 */
	public RefinementNode(GeoLiftModule module, double fitness, Model inputModel, Model outputModel, 
			Resource inputDataset, Resource outputDataset, Model configModel) {
		super();
		this.module = module;
		this.fitness = fitness;
		this.inputModel = inputModel;
		this.outputModel = outputModel;
		this.configModel = configModel;
		this.inputDataset = inputDataset;
		this.outputDataset = outputDataset;
		if(configModel != null){
			configModel.setNsPrefix("gl", SpecsOntology.uri);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
				return module.getClass().getSimpleName() + "(" + fitness +")"; 
//				"\n fitness=" + fitness +
//				"\n outputModel(" + output.size() + ")=" + 
//				outputModel.write(System.out,"TTL") + 
//				"\n configModel(" + config.size() + ")="; 
//+ 
//				configModel.write(System.out,"TTL") + 
//				",\n childNr=" + childNr + "]";
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RefinementNode o) {
		return (int) (fitness - o.fitness);
//		if(fitness > o.fitness){
//			return 1;
//		} else if(fitness < o.fitness){
//			return -1;
//		}else 
//			return 0;
	}
}

//class ExecutionNodeComp implements Comparator<ExecutionNode>{
//	/* (non-Javadoc)
//	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
//	 */
//	@Override
//	public int compare(ExecutionNode e1, ExecutionNode e2) {
//		if(e1.fitness > e2.fitness){
//			return 1;
//		} else if(e1.fitness < e2.fitness){
//			return -1;
//		}else 
//			return 0;
//	}
//}