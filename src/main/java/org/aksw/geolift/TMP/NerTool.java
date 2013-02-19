/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

import java.util.TreeSet;


/**
 * This interface implements a wrapper around NER tools. It takes the input text
 * as input and returns a set of named entities 
 * @author ngonga
 */
public interface NerTool {

    /** Returns a set of named entities, in general including their type and offset
     * 
     * @param input Input text to be sent to the NER tool
     * @return Set of named entities
     */
    public TreeSet<NamedEntity> getNEs(String input);
    public TreeSet<NamedEntity> getNEs(Configuration c);
}
