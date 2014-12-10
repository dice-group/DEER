/**
 * 
 */
package org.aksw.deer.helper.vacabularies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class SPECS {
	public static final String uri = "http://geoknow.org/specsontology/";
	public static final String prefix = "DEER";

	private static Property property(String name) {
		Property result = ResourceFactory.createProperty(uri + name);
		return result;
	}

	protected static final Resource resource( String local ){ 
		return ResourceFactory.createResource( uri + local ); 
	}

	public static String getURI(){ return uri;	}

	public static final Property hasUri	= property("hasUri");
	public static final Property inputFile	= property("inputFile");
	public static final Property outputFile	= property("outputFile");
	public static final Property outputFormat	= property("outputFormat");
	public static final Property FromEndPoint	= property("FromEndPoint");
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
	
//	public static final Resource Step 						= resource( "Step" );
	public static final Resource Dataset 						= resource( "Dataset" );
	public static final Resource Module 						= resource( "Module" );
	public static final Resource DereferencingModule 			= resource( "DereferencingModule" );
	public static final Resource LinkingModule 				= resource( "LinkingModule" );
	public static final Resource NLPModule 					= resource( "NLPModule" );
	public static final Resource FilterModule 				= resource( "FilterModule" );
	public static final Resource AuthorityConformationModule 	= resource( "AuthorityConformationModule" );
	public static final Resource PredicateConformationModule 	= resource( "PredicateConformationModule" );
	public static final Resource ModuleParameter 				= resource( "ModuleParameter" );
	public static final Resource NLPModuleParameter 			= resource( "NLPModuleParameter" );
	public static final Resource DereferencingModuleParameter	= resource( "DereferencingModuleParameter" );
	public static final Resource LinkingModuleParameter 		= resource( "linkingModuleParameter" );
	public static final Resource FilterModuleParameter 		= resource( "FilterModuleParameter" );
	public static final Resource Operator 					= resource( "Operator" );
	public static final Resource CloneOperator 				= resource( "CloneOperator" );
	public static final Resource MergeOperator 				= resource( "MergeOperator" );
	public static final Resource OperatorParameter 			= resource( "OperatorParameter" );
	public static final Resource CloneOperatorParameter 		= resource( "CloneOperatorParameter" );
	public static final Resource MergeOperatorParameter 		= resource( "MergeOperatorParameter" );
	public static final Resource AuthorityConformationModuleParameter 	= resource( "AuthorityConformationModuleParameter" );
	public static final Resource PredicateConformationModuleParameter 	= resource( "PredicateConformationModuleParameter" );
	
}
