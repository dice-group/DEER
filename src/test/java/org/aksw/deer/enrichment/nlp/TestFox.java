/**
 *
 */
package org.aksw.deer.enrichment.nlp;

import org.aksw.fox.binding.java.FoxApi;
import org.aksw.fox.binding.java.FoxParameter.OUTPUT;
import org.aksw.fox.binding.java.FoxResponse;
import org.aksw.fox.binding.java.IFoxApi;

/**
 * @author sherif
 *
 */
public class TestFox {

    public static void main(String []a){

        example_two();

    }
    public static void example_two() {

        IFoxApi fox = new FoxApi();

        FoxResponse response = fox.
                setInput("The philosopher and mathematician" +
                        " Gottfried Wilhelm Leibniz was born in Leipzig in 1646 and attended the University of Leipzig from 1661-1666.")
                .setOutputFormat(OUTPUT.TURTLE).

                        send();

        System.out.println("INPUT: " +response.getInput());
        System.out.println("OUTPUT: " +response.getOutput());
        System.out.println("LOG: " +response.getLog());
    }
}
