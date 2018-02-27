## Extending DEER

DEER is built on top of [FARADAY-CAGE](https://github.com/dice-group/faraday-cage), which uses the
plugin system [PF4J](https://github.com/decebals/pf4j).  
This is good news for developers willing to extend DEER with their custom enrichment operators,
as you do not need to fork and mess with DEER source directly.
Instead, you just need to follow a few guidelines and implement an easy interface to be ready to
plug your enrichment operator into DEER.  

We define three base interfaces:
 
 1. `org.aksw.deer.DeerPlugin` 
 2. `org.aksw.deer.ParametrizedDeerPlugin`
 3. `org.aksw.deer.learning.SelfConfigurator`
 
and four abstract classes:

 1. `org.aksw.deer.enrichments.AbstractEnrichmentOperator`
 2. `org.aksw.deer.enrichments.AbstractParametrizedEnrichmentOperator`
 3. `org.aksw.deer.io.AbstractModelReader`
 4. `org.aksw.deer.io.AbstractModelWriter`
 
You should always extend the abstract classes and never need to implement the interfaces directly,
but they could come in handy when trying to obtain instances programmatically using 
`org.aksw.faraday-cage.PluginFactory`.

For more information on how to extend these classes, please read the Javadoc.  
 *Note that javadoc documentation does currently not cover a lot of the code base. This will
 be corrected for by the time we release version 1.0.0.* 

An example plugin using a parameterless enrichment
operator can be found [here](https://github.com/dice-group/deer/tree/master/examples/simple-plugin-example/).

## Parametrization of Plugins

We are currently extending FARADAY-CAGE with a completely standardized method to parametrize nodes,
including full automatic parameter subgraph validation and self-documentation.
Until then, the API for parametrized enrichment operators is not considered final and therefore,
we will not document it now.

--- 