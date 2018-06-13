# Configuring DEER

DEER is configured using a simple RDF vocabulary.
Its namespace is `http://deer.aksw.org/vocabulary/#`.

There are just three predefined predicates that DEER inherits from FARADAY-CAGE:

* `<http://deer.aksw.org/vocabulary/#implementedIn>`
* `<http://deer.aksw.org/vocabulary/#hasInput>`
* `<http://deer.aksw.org/vocabulary/#hasOutput>`

To learn about the usage of these predicates please read the documentation on the [FARADAY-CAGE core vocabulary](https://dice-group.github.io/faraday-cage/CONF.html#core). 
Plugins are associated with unique resources. The [default plugins](./configuring_deer/enrichment_operators.md) that ship with deer-core live in
the deer namespace and have just their class name as local part, e.g.: 

* `<http://deer.aksw.org/vocabulary/#DefaultModelReader>`
* `<http://deer.aksw.org/vocabulary/#DefaultModelWriter>`
* `<http://deer.aksw.org/vocabulary/#FilterEnrichmentOperator>`

Custom plugins should be identified by resources outside of the default namespace to prevent
naming collisions.

Plugins define their own configuration vocabulary. [Here](./configuring_deer/enrichment_operators.md), you can find an accurate description of the available parameters of all the predefined enrichment operators in the current release.

The following example configuration demonstrates how the predefined vocabulary works:  

```turtle
@prefix : <http://deer.aksw.org/vocabulary/#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .

:node_reader1
              :implementedIn     :DefaultModelReader ;
              :fromUri           <http://de.dbpedia.org/resource/Paderborn> ;
              :useEndpoint       <http://de.dbpedia.org/sparql> ;
              :hasOutput         ( :node_conf ) .

:node_reader2
              :implementedIn     :DefaultModelReader ;
              :fromUri           <http://dbpedia.org/resource/Paderborn> ;
              :useEndpoint       <http://dbpedia.org/sparql> ;
              :hasOutput         ( :node_geofusion ) .

:node_conf
              :implementedIn     :AuthorityConformationEnrichmentOperator ;
              :hasInput          ( :node_reader1 ) ;
              :hasOutput         ( :node_geofusion ) ;
              :sourceSubjectAuthority
                                 "http://dbpedia.org" ;
              :targetSubjectAuthority
                                 "http://deer.org" .

:node_geofusion
              :implementedIn     :GeoFusionEnrichmentOperator ;
              :hasInput          ( :node_conf :node_reader2 ) ;
              :hasOutput         ( :node_filter ) ;
              :fusionAction      "takeAll" ;
              :mergeOtherStatements
                                 "true" .

:node_filter
              :implementedIn     :FilterEnrichmentOperator ;
              :hasInput          ( :node_geofusion ) ;
              :hasOutput         ( :node_writer ) ;
              :selectors         (
                    [ :predicate geo:lat ]
                    [ :predicate geo:long ]
                    [ :predicate rdfs:label ]
                    [ :predicate owl:sameAs ]
              ) .

:node_writer
              :implementedIn     :DefaultModelWriter ;
              :outputFile        "output_demo.ttl" ;
              :outputFormat      "Turtle" ;
:hasInput ( :node_filter ) .
```
