/**
 * 
 */
package org.aksw.deer.helper.datastructure;

/**
 * @author sherif
 *
 */
public class FMeasure {


	public double P,R,F;
	
	/**
	 * 
	 *@author sherif
	 */
	public FMeasure(double p, double r, double f) {
		P = p;
		R = r;
		F = f;
	}
	

	@Override
	public String toString() {
		return "FMeasure [P=" + P + ", R=" + R + ", F=" + F + "]";
	}

}
