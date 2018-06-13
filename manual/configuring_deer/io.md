# Predefined IO

## DefaultModelReader

The DefaultModelReader can read in RDF from any file format supported by Apache Jena as well as from querying
SPARQL endpoints.  
Its supported configuration parameters are:

 * `:useEndpoint` if this is given, DefaultModelReader will operate in *endpoint mode* and the endpoint will be set to the value of this parameter.
 * `:fromUri` in *endpoint mode*, issue a **DESCRIBE** query for the given uri; in *normal mode* read in rdf directly from the given uri (can also be a file path for local files).
 * `:useSparqlConstruct` issue the **CONSTRUCT** query given as value of this parameter against either the model obtained by reading in the RDF in *normal mode* or the SPARQL endpoint in *endpoint mode*.
 In *endpoint mode*, this has precedence over `:fromUri`, i.e. if both are specified, only the **CONSTRUCT** query is issued.    


## DefaultModelWriter

The DefaultModelWriter can write RDF to any file format supported by Apache Jena.  
Its supported configuration parameters are:

 * `:outputFile` *(required)* specifies the name of the emitted file
 * `:outputFormat`  specifies the file format to be used, defaults to Turtle.
 Supports [all formats of Apache Jena](https://jena.apache.org/documentation/io/rdf-output.html#jena_model_write_formats).