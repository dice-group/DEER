# DEER Documentation

## Motivation

Over the last years, the Linked Data principles have been used across academia and industry
to publish and consume structured data.
Thanks to the fourth Linked Data principle, many of the RDF datasets used within these applications
contain implicit and explicit references to more data.
For example, music datasets such as Jamendo include, amongst others, references to locations of
record labels, places where artists were born or have been.
Datasets such as Drugbank contain references to drugs from DBpedia, were verbal description of the
drugs and their usage is explicitly available.

The RDF Dataset Enrichment Framework (DEER), is a modular and highly
extensible software system for dataset manipulation and verification.
DEER has its own configuration language, which describes dataset enrichment
tasks as directed acyclic graphs of enrichment operators.
The configuration language is implemented as RDF vocabulary for
maintainability and retrievability from SPARQL endpoints which can 
in effect be used as configuration repositories.

DEER can run an existing configuration graph from e.g. a file
on your local disk or an URL.
More useful for large scale dataset enrichment, however, is DEERs ability
to employ supervised machine learning to automatically assemble
configuration graphs for your needs.

Users of DEER can get familiar with the installation procedure, the CLI,
the configuration language, existing enrichment operators and the machine
learning capabilities in our [user manual](./user/).

Developers can find instructions on how to extend deer with new enrichment
operators in the [dev manual](./dev/).

*THIS DOCUMENTATION IS STILL UNDER CONSTRUCTION*  
*In the meantime please refer to the official [javadoc](./javadoc/)*

```
String s = "";
``` 
