/**
 * 
 */
package org.aksw.geolift.workflow.rdfspecs;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class SpecsOntology {
	public static final String uri = "http://geoknow.org/specsontology/";

	private static Property property(String name) {
		Property result = ResourceFactory.createProperty(uri + name);
		return result;
	}

	protected static final Resource resource( String local ){ 
		return ResourceFactory.createResource( uri + local ); 
	}

	public static String getURI(){ return uri;	}

	public static final Property hasUri	= property("hasUri");
	public static final Property hasParameter	= property("hasParameter");
	public static final Property hasKey 		= property("hasKey");
	public static final Property hasValue 	= property("hasValue");
	public static final Property hasModule 	= property("hasModule");
	public static final Property hasOperator 	= property("hasOperator");
	public static final Property hasInput 	= property("hasInput");
	public static final Property hasOutput 	= property("hasOutput");
	public static final Property nextStep 	= property("nextStep");
	public static final Property isFirstStep 	= property("isFirstStep");
	public static final Property isLastStep 	= property("isLastStep");
	
	public static final Resource Step 						= resource( "Step" );
	public static final Resource Dataset 						= resource( "Dataset" );
	public static final Resource Module 						= resource( "Module" );
	public static final Resource DereferencengModule 			= resource( "DereferencengModule" );
	public static final Resource LinkingModule 				= resource( "LinkingModule" );
	public static final Resource NLPModule 					= resource( "NLPModule" );
	public static final Resource FilterModule 				= resource( "FilterModule" );
	public static final Resource ConformationModule 			= resource( "ConformationModule" );
	public static final Resource ModuleParameter 				= resource( "ModuleParameter" );
	public static final Resource NLPModuleParameter 			= resource( "NLPModuleParameter" );
	public static final Resource DereferencengModuleParameter	= resource( "DereferencengModuleParameter" );
	public static final Resource LinkingModuleParameter 		= resource( "linkingModuleParameter" );
	public static final Resource FilterModuleParameter 		= resource( "FilterModuleParameter" );
	public static final Resource ConformationModuleParameter 	= resource( "ConformationModuleParameter" );
	public static final Resource Operator 					= resource( "Operator" );
	public static final Resource SplitOperator 				= resource( "SplitOperator" );
	public static final Resource MergeOperator 				= resource( "MergeOperator" );
	
}
