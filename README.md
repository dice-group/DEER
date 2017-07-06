DEER
=======

Over the last years, the Linked Data principles have been used across academia and industry to publish and consume structured data. Thanks to the fourth Linked Data principle, many of the RDF datasets used within these applications contain implicit and explicit references to more data. For example, music datasets such as Jamendo include references to locations of record labels, places where artists were born or have been, etc. Datasets such as Drugbank contain references to drugs from DBpedia, were verbal description of the drugs and their usage is explicitly available.
The goal of mapping component, dubbed DEER, is to retrieve this information, make it explicit and integrate it into data sources according to the specifications of the user. To this end, DEER relies on a simple yet powerful pipeline system that consists of two main components: modules and operators.

Modules implement functionality for processing the content of a dataset (e.g., applying named entity recognition to a
particular property). Thus, they take a dataset as input and return a dataset as output.
Operators work at a higher level of granularity and combine datasets. Thus, they take sets of datasets as input and return sets of datasets.

DEER implemented in Java as an open-source project. Please see /DEER_Manual/DEER_manual.pdf for all the technical details

## Building DEER using Maven

To build DEER using Maven, simply do
```
mvn clean compile assembly:assembly
```


