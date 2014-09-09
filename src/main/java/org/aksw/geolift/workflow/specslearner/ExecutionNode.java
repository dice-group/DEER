/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import org.aksw.geolift.modules.GeoLiftModule;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class ExecutionNode {
	
	public GeoLiftModule module;
	public double fitness;
	public Model outputModel;
	public Model configModel;
	public int childNr;

	/**
	 * 
	 *@author sherif
	 */
	public ExecutionNode() {
		fitness = 0;
	}

	/**
	 * @param module
	 * @param fitness
	 * @param outputModel
	 *@author sherif
	 */
	public ExecutionNode(GeoLiftModule module, long fitness, Model outputModel, Model configModel,int childNr) {
		super();
		this.module = module;
		this.fitness = fitness;
		this.outputModel = outputModel;
		this.configModel = configModel;
		this.childNr = childNr;
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
