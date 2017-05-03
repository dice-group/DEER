/**
 *
 */
package org.aksw.deer.helper.kb;

import org.aksw.deer.workflow.specslearner.SimplePipeLineLearner;
import org.apache.log4j.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 *
 */
public class KBSampler {
    static final Logger logger = Logger.getLogger(SimplePipeLineLearner.class.getName());

    public Model getCBD(Resource r, Model m){
        logger.info("Generating CBD of Resource " + r);
        String sparqlQueryString =
                "DESCRIBE <" + r + ">";
        QueryFactory.create(sparqlQueryString);
        QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, m);
        Model cbd = qexec.execDescribe();
        qexec.close();
        return cbd;
    }

}
