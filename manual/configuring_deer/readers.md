# Predefined ModelReaders

## File Model Reader (`deer:FileModelReader`) {#file}
This Model Reader can read in RDF from any file format supported by Apache Jena,
either living on your hard disk or fetchable from the web.
Choose one of its configuration parameters to select how it should operate:

 * `deer:fromUri` if given, will operate in *web mode* and try to fetch the
 configuration from the given URI resource.
 * `deer:fromPath` if given, will operate in *local filesystem mode* and try to fetch the
 configuration from the given file path string literal.
 
## SPARQL Model Reader (`deer:SparqlModelReader`) {#sparql}
This Model Reader can read in RDF from a SPARQL endpoint.
It can operate in two modes:
 1. **CONSTRUCT mode**: Takes a SPARQL CONSTRUCT query as configuration and issues the query to
 the configured endpoint. 
 2. **DESCRIBE mode**: Takes a resource as configuration and issues a SPARQL DESCRIBE query for it to
 the configured endpoint.

Its configuration parameters are:

 * `deer:fromEndpoint` *(required)* the URI resource of the endoint.
 * `deer:useSparqlDescribeOf` URI resource to query with SPARQL DESCRIBE
 * `deer:useSparqlConstruct` SPARQL CONSTRUCT query to be issued to the endpoint