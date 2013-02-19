/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.geolift.TMP;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.ByteArrayInputStream;
import java.util.TreeSet;

/**
 *
 * @author ngonga
 */
public class TurtleReader {

    public static final String ANN = "http://www.w3.org/2000/10/annotation-ns#";
    public static final String SCMS = "http://ns.aksw.org/scms/";
    public static final String SCMSANN = "http://ns.aksw.org/scms/annotations/";
    public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static TreeSet<NamedEntity> read(String input) {
        
        TreeSet<NamedEntity> nes = new TreeSet<NamedEntity>();
        Model model = ModelFactory.createDefaultModel();
        //StringReader reader = new StringReader(input);
        ByteArrayInputStream stream;
        try {
            stream = new ByteArrayInputStream(input.getBytes("UTF-8"));
            model.read(stream, "", "TTL");
            model.write(System.out,"TTL");
            //System.out.println(model);
            //get properties
            Property body = model.getProperty(ANN, "body");
            Property index = model.getProperty(SCMS, "beginIndex");
            Property subclass = model.getProperty(RDF, "type");

            ResIterator nodeIter = model.listResourcesWithProperty(body);
            String label, offset, type, types;
            while (nodeIter.hasNext()) {
                type = null;
                //get label
                RDFNode n = nodeIter.next();
                NodeIterator iter = model.listObjectsOfProperty(n.asResource(), body);                
                label = iter.next().toString();
                label = label.substring(0, label.lastIndexOf("^^http://www.w3.org/2001/XMLSchema#string"));
                //get type
                iter = model.listObjectsOfProperty(n.asResource(), subclass);                
                while(iter.hasNext())
                {
                    types = iter.next().toString();
                    if(types.contains(SCMSANN))
                    type = types.substring(types.lastIndexOf("/")+1);
                }
                //get indexes
                iter = model.listObjectsOfProperty(n.asResource(), index);
                                                                              

                while (iter.hasNext()) {
                    offset = iter.next().toString();
                    offset = offset.substring(0, offset.lastIndexOf("^^http://www.w3.org/2001/XMLSchema#int"));
                    nes.add(new NamedEntity(label, type, Integer.parseInt(offset)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // write it to standard out        
        return nes;
    }
}
