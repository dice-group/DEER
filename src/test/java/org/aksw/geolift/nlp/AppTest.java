package org.aksw.geolift.nlp;
import java.util.List;

import com.hp.hpl.jena.vocabulary.RDFS;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * An even more rigourous Test :-)
     */
    public void testApp()
    {
        NlpGeoEnricher app = new NlpGeoEnricher("demo.ttl");
        List<String> hellos = app.queryForPropertyValues(RDFS.label);
        
        assertTrue(hellos.size()==1);
        assertTrue("Hello World".equals(hellos.get(0)));
    }
}