/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements the mapping of a named entity to URIs
 * @author ngonga
 */
public class EntityUriMap extends HashMap<NamedEntity, HashMap<String, Double>> {

    public TreeSet<NamedEntity> getAllEntityLabels() {
        return (TreeSet) keySet();
    }

    /** Get all uris contained in this map
     * 
     * @return Set of URIs
     */
    public TreeSet<String> getAllUris() {
        TreeSet<String> allUris = new TreeSet<String>();
        Set<String> uris;
        for (NamedEntity e : keySet()) {
            uris = get(e).keySet();
            for (String uri : uris) {
                allUris.add(uri);
            }
        }
        return allUris;
    }
    
    /** 
     * 
     * @return  A string representation of the object
     */
    public String toString()
    {
        String result = "";
        TreeSet<String> uris = getAllUris();
        
        for(String s: uris)
        {
            result = result + "\t" + s.substring(s.lastIndexOf("/")+1);
        }
        
        result = result +"\n";
        for(NamedEntity e: keySet())
        {
            result = result + e.label+"\t";
            for(String s: uris)
            {
                if(get(e).containsKey(s))
                {
                    result = result + get(e).get(s) +"\t";
                }
                else
                    result = result +"0\t";
            }
            result = result + "\n";
        }
        return result;
    }

    /** Convenience method to add a named entity, the corresponding uri and a score
     * 
     * @param e Named entity
     * @param uri Correspoding uri
     * @param score Corresponding score
     */
    public void add(NamedEntity e, String uri, double score) {
        if (!containsKey(e)) {
            put(e, new HashMap<String, Double>());
        }
        get(e).put(uri, score);
    }
    
    /** Returns get(e).get(uri) if it exists, else 0
     * 
     * @param e Named Entity
     * @param uri URI
     * @return Score
     */
    public double get(NamedEntity e, String uri)
    {
        if(containsKey(e))
        {
            if(get(e).containsKey(uri))
            {
                return get(e).get(uri).doubleValue();
            }
            else return 0;
        }
        else return 0;
    }
    
    public HashMap<NamedEntity, String> getBestMapping()
    {
        HashMap<NamedEntity, String> result = new HashMap<NamedEntity, String>();
        double max, sim;
        HashMap<String, Double> map;
        for(NamedEntity e: keySet())
        {
            max = -1;
            map = get(e);
            if(map.isEmpty()) result.put(e, "*"+e.label);
            for(String uri: map.keySet())
            {
                sim = map.get(uri);
                if(sim > max)
                {
                    result.put(e, uri);
                    max = sim;
                }
            }
        }
        return result;
    }
}
