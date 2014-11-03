/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.io.IOException;

import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.workflow.Deer;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class Evaluation {
	
	public static void run(String args[], boolean isBatch, int max) throws IOException{
		String folder = args[0];
		String results = "ModuleCount\tTime\tTreeSize\tIterationNr\tP\tR\tF\n";
		for(int i = 1 ; i <= max; i++){
			SpecsLearn learner = new SpecsLearn();
			if(isBatch){
				folder = folder + i;
			}
			learner.sourceModel  = Reader.readModel(folder + "/input.ttl");
			learner.targetModel  = Reader.readModel(folder + "/output.ttl");
			long start = System.currentTimeMillis();
			RefinementNode bestSolution = learner.run();
			long end = System.currentTimeMillis();
			long time = end - start;
			results += i + "\t" + time + "\t" + 
					learner.refinementTreeRoot.size() + "\t" + 
					learner.iterationNr + "\t" + 
//					bestSolution.fitness + "\t" +
					learner.computePrecision(bestSolution.outputModel, learner.targetModel) + "\t" + 
					learner.computeRecall(bestSolution.outputModel, learner.targetModel) + "\t" +
					learner.computeFMeasure
					(bestSolution.outputModel, learner.targetModel);
			Writer.writeModel(bestSolution.configModel, "TTL", folder + "/self_config.ttl");
//			bestSolution.outputModel.write(System.out,"TTL");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(results);
//			break;
		}
		System.out.println(results);
	}
	
	
	public void evaluateEnrichment(Model manual, Model selfConfig){
		Deer deer = new Deer();
	}
	
	public static void main(String args[]) throws IOException{
		run(args, false, 1);
	}

}
