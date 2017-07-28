/**
 *
 */
package org.aksw.deer.learning;


import org.aksw.deer.vocabulary.SPECS;
import org.aksw.deer.util.IEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * @author sherif
 */
public class RefinementNodeOld implements Comparable<RefinementNodeOld> {

  public IEnrichmentOperator module = null;
  public double fitness = -Double.MAX_VALUE;
  public Model inputModel = ModelFactory.createDefaultModel();
  public Model outputModel = ModelFactory.createDefaultModel();
  public Model configModel = ModelFactory.createDefaultModel();
  public Resource inputDataset = ResourceFactory.createResource();
  public Resource outputDataset = ResourceFactory.createResource();
  public NodeStatus status;

  /**
   * @author sherif
   */
  public RefinementNodeOld() {
    super();
    configModel.setNsPrefix("gl", SPECS.uri);
  }

  public RefinementNodeOld(double fitness) {
    this();
    this.fitness = fitness;
  }

  /**
   * @author sherif
   */
  public RefinementNodeOld(IEnrichmentOperator module, double fitness, Model inputModel, Model outputModel,
    Resource inputDataset, Resource outputDataset, Model configModel) {
    super();
    this.module = module;
    this.fitness = fitness;
    if (fitness == -2) {
      status = NodeStatus.DEAD;
    }
    this.inputModel = inputModel;
    this.outputModel = outputModel;
    this.configModel = configModel;
    this.inputDataset = inputDataset;
    this.outputDataset = outputDataset;
    if (configModel != null) {
      configModel.setNsPrefix("gl", SPECS.uri);
    }
  }


  public RefinementNodeOld(IEnrichmentOperator operator, Model inputModel, Model outputModel,
    Resource inputDataset, Resource outputDataset, Model configModel) {
    super();
    if (fitness == -2) {
      status = NodeStatus.DEAD;
    }
    this.inputModel = inputModel;
    this.outputModel = outputModel;
    this.configModel = configModel;
    this.inputDataset = inputDataset;
    this.outputDataset = outputDataset;
    if (configModel != null) {
      configModel.setNsPrefix("gl", SPECS.uri);
    }
  }

  /**
   * @author sherif
   */
  public static void main(String[] args) {

  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return module.getClass().getSimpleName() + "(" + fitness + ")";
//				"\n fitness=" + fitness +
//				"\n outputModel(" + output.size() + ")=" +
//				outputModel.write(System.out,"TTL") +
//				"\n configModel(" + config.size() + ")=";
//+
//				configModel.write(System.out,"TTL") +
//				",\n childNr=" + childNr + "]";
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(RefinementNodeOld o) {
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