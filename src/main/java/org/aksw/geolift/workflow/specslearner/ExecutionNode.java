/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.Comparator;

import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.workflow.rdfspecs.SpecsOntology;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author sherif
 *
 */
public class ExecutionNode implements Comparable<ExecutionNode> {

	public GeoLiftModule module;
	public double fitness;
	public Model input = ModelFactory.createDefaultModel();
	public Model output = ModelFactory.createDefaultModel();
	public Model config = ModelFactory.createDefaultModel();
//	public int childNr;

	/**
	 * 
	 *@author sherif
	 */
	public ExecutionNode() {
		super();
		fitness = 0;
		config.setNsPrefix("gl", SpecsOntology.uri);
	}

	/**
	 * @param module
	 * @param fitness
	 * @param outputModel
	 * @param configModel
	 * @param childNr
	 *@author sherif
	 */
	public ExecutionNode(GeoLiftModule module, long fitness, Model inputModel, Model outputModel, Model configModel) {
		super();
		this.module = module;
		this.fitness = fitness;
		this.input = inputModel;
		this.output = outputModel;
		this.config = configModel;
		configModel.setNsPrefix("gl", SpecsOntology.uri);
//		this.childNr = childNr;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
				return module.getClass().getSimpleName(); 
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
	public int compareTo(ExecutionNode o) {
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