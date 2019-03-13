# Predefined DEER Execution Node Wrappers

## SPARQL Analytics Wrapper (`deer:SparqlAnalyticsWrapper`) {#sparql-analytics}
This wrapper can derive analytics information from an execution nodes input and/or output models via
a SPARQL SELECT query and attach them to the DEER *JSON* analytics output file.
Its configuration parameters are:

 * `deer:sparqlSelectQuery` *(required)* a SPARQL SELECT query over the input and output models.  
 Input and output models are put in their own named graphs, i.e. for an execution node with
 2 inputs and 2 outputs there will be the following named graphs available:
  * `deer:inputGraph0`
  * `deer:inputGraph1`
  * `deer:outputGraph0`
  * `deer:outputGraph1`  
 
 
 * `deer:jsonOutput` *(required)* a JSON object to be attached to every decorated execution nodes analytics
 information. The variables from `deer:sparqlSelectQuery` are used as placeholders.  
 *More refined placeholder syntax will be available in future releases.*