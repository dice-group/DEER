/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

/**
 * Maps entity labels to URI
 * @author ngonga
 */
public class EntityUriMapping {
    NamedEntity entity;
    String uri;
    double score;
    
    /** Stores the mapping of an entity to a given uri
     * 
     * @param _entity Entity whose URI in the reference knowledge base is to be stored
     * @param _uri URI of the entity
     * @param _score Confidence w.r.t. the assignment
     */
    public EntityUriMapping(NamedEntity _entity, String _uri, double _score)
    {
        entity = _entity;
        uri = _uri;
        score = _score;
    }
    

}
