/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.aksw.deer.operators.fusion;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Computes the precision, recall and F-score
 * @author sherif
 */
public class PRFComputer {


	/**
	 * compute overlap between result m and a reference 
	 * @param m
	 * @param reference
	 * @return
	 * @author sherif
	 */
	public synchronized static double getOverlap(final Collection<OWLIndividual> m, final Collection<OWLIndividual> reference){
		double counter = 0;
		for(OWLIndividual i : m){ 
			if(reference.contains(i) ){
				counter++;
			}
		}
		return counter;
	}



	/**
	 * Computes the precision of the mapping m with respect to the reference mapping
	 * @param m
	 * @param reference
	 * @return Precision
	 * @author sherif
	 */
	public static double computePrecision(final Collection<OWLIndividual> m, final Collection<OWLIndividual> reference){
		if(m.size()<=0)
			return 0d;
		return getOverlap(m, reference)/(double)m.size();
	}

	/**
	 * Computes the recall of the mapping m with respect to the reference mapping
	 * @param m
	 * @param reference
	 * @return Recall
	 * @author sherif
	 */
	public static double computeRecall(final Collection<OWLIndividual> m, final Collection<OWLIndividual> reference){
		return getOverlap(m, reference)/(double)reference.size();
	}

	/**
	 * Computes the F1-score of the mapping m with respect to the reference mapping
	 * @param m
	 * @param reference
	 * @return F1-score
	 * @author sherif
	 */
	public static double computeFScore(final Collection<OWLIndividual> m, final Collection<OWLIndividual> reference){
		if(m.size() == 0) {
			return 0d;
		}
		double overlap = getOverlap(m, reference);
		return 2*(overlap/(double)m.size())*(overlap/(double)reference.size())/(overlap/(double)m.size()+(overlap/(double)reference.size()));
	}
}
