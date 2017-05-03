/**
 *
 */
package org.aksw.deer.helper.datastructure;

import org.apache.jena.rdf.model.Model;

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


    public static FMeasure computePRF(Model current, Model target){
        double p = computePrecision(current, target);
        double r = computeRecall(current, target);
        double f = 2 * p * r / (p + r);
        return new FMeasure(p, r, f);
    }

    public static double computeFMeasure(Model current, Model target){
        double p = computePrecision(current, target);
        double r = computeRecall(current, target);
        return 2 * p * r / (p + r);
    }

    public static double computePrecision(Model current, Model target){
        return (double) current.intersection(target).size() / (double) current.size();
    }

    public static double computeRecall(Model current, Model target){
        return (double) current.intersection(target).size() / (double) target.size();
    }

    @Override
    public String toString() {
        return "FMeasure [P=" + P + ", R=" + R + ", F=" + F + "]";
    }

}
