/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains the solution to a mapping problem
 * @author ngonga
 */
public class UriMappingSet {
    private Set<EntityUriMapping> mappingSet;
    
    /** Get all entity labels
     * 
     * @return 
     */
    public TreeSet<NamedEntity> getAllEntityLabels()
    {
        TreeSet<NamedEntity> result = new TreeSet<NamedEntity>();
        for(EntityUriMapping m: mappingSet)
        {
            result.add(m.entity);
        }
        return result;
    }
    /** Initializes a solution
     * 
     */
    public UriMappingSet()
    {
        mappingSet = new TreeSet<EntityUriMapping>();
    }
    
    /** Ensures that the mapping is unique
     * 
     * @param mapping Mapping to be added to the solution
     */
    public void add(EntityUriMapping mapping)
    {
       mappingSet.add(mapping);
    }
    
    /** Returns all the mapping uris for a given label
     * 
     * @param label Label to look up
     * @return All mapping URIs including their score
     */
    public HashMap<String, Double> getScoredUris(String label)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(EntityUriMapping m: mappingSet)
        {
            if(m.entity.label.equals(label)) result.put(m.uri, m.score);
        }
        return result;
    }
    
    /** Gets all URIs and scores for the entity e
     * 
     * @param e Entity, whose URIs are being looked for
     * @return Hashmap of URIs and scores
     */
    public HashMap<String, Double> getScoredUris(NamedEntity e)
    {
        HashMap<String, Double> result = new HashMap<String, Double>();
        for(EntityUriMapping m: mappingSet)
        {
            if(m.entity.label.equals(e.label) && (m.entity.offset == e.offset)) result.put(m.uri, m.score);
        }
        return result;
    }
    /** Returns the best mapping of each of the named entities to a URI
     * 
     * @return Best mapping of each named entity to a URI
     */
    public HashMap<NamedEntity, String> getBestMapping()
    {
        HashMap<NamedEntity, EntityUriMapping> bestMapping = new HashMap<NamedEntity, EntityUriMapping>();
        for(EntityUriMapping e: mappingSet)
        {
            if(bestMapping.containsKey(e.entity))
            {
                if (bestMapping.get(e.entity).score < e.score)
                    bestMapping.put(e.entity, e);
            }
            else
                bestMapping.put(e.entity, e);
        }
        
        HashMap<NamedEntity, String> result = new HashMap<NamedEntity, String>();
        for(NamedEntity e: bestMapping.keySet())
        {
            result.put(e, bestMapping.get(e).uri);
        }
        
        return result;
    }
    
    /** 
     * Returns all URIs contained in the mapping
     * @returns Set of all URIs in the mapping
     */
    public TreeSet<String> getAllUris()
    {
        TreeSet<String> allUris = new TreeSet<String>();
        for(EntityUriMapping m: mappingSet)
        {
            allUris.add(m.uri);
        }        
        return allUris;
    }
}
