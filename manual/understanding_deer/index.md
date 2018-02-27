# About DEER

## Architecture

<img src="./deer_stack.png" style="float:right; margin:0em 0em 2em 2em"/>

DEERs former execution engine has been outsourced to FARADAY-CAGE, a framework that provides
abstractions for nodes in a directed acyclic graph that represent computation steps on more or
less homogeneous data structures which are in turn represented by the graphs edges.

This kind of graph is called execution graph.
It is dynamically built from an RDF specification by use of PF4J's plugin mechanism.
Graph nodes in FARADAY-CAGE are PF4J extension points, which means that every step in the execution
graph is performed by a plugin that is dynamically loaded and automatically parametrized from
the RDF configuration.
FARADAY-CAGE will also automatically parallelize plugins based on the graphs structure.

DEER itself consists of two modules: deer-core and deer-server.
The first is a library that implements a domain model for dataset enrichment on top of FARADAY-CAGE
while the latter provides a CLI that can either run a single configuration or spawn a server to 
submit tasks over a RESTful API. In a future release, the API will be accompanied by a web app
to build configurations graphically in the browser.

## Plugin Types

DEERs domain model specifies three basic plugin types:

* A **ModelReader** is the only plugin that can be a root node in the execution graph and likewise it
must be a root node. It is responsible for reading one RDF dataset from a dedicated source to feed it into 
the exxecution graph.  
* A **ModelWriter** can be an intermediary or a leaf node and it will write one RDF dataset to an external channel,
e.g. a file or a triplestore.  
* An **EnrichmentOperator** is an intermediary node that takes one or more RDF datasets as input,
executes arbitrary transformations on them and outputs the result as one or more RDF datasets.