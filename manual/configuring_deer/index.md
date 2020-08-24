# Configuring DEER

DEER is configured using the 
[**FARADAY-CAGE** Configuration Vocabulary](https://dice-group.github.io/faraday-cage/conf.html).
The predefined plugins that ship with *deer-core* as well as their parameters live in the
`http://w3id.org/deer/` namespace with the canonical prefix `deer:`.

Predefined plugins are denominated by their class name, e.g.

* `<http://w3id.org/deer/FileModelReader>` or `deer:FileModelReader`
* `<http://w3id.org/deer/FileModelWriter>` or `deer:FileModelWriter`
* `<http://w3id.org/deer/FilterEnrichmentOperator>` or `deer:FilterEnrichmentOperator`

The parameter vocabulary of our predefined plugins is described more precisely in the following
sections of this manual.

Custom plugins should be identified by resources outside of the default namespace to prevent
naming collisions.

The following example configuration demonstrates how the predefined vocabulary works:  

```turtle
@prefix : <urn:example:demo/> .
@prefix fcage: <http://w3id.org/fcage/> .
@prefix deer: <http://w3id.org/deer/> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

:node_reader1_dbp
  a deer:SparqlModelReader ;
  deer:useSparqlDescribeOf <http://dbpedia.org/resource/Paderborn> ;
  deer:fromEndpoint       <http://dbpedia.org/sparql> ;
.

:node_reader_lgd
  a deer:SparqlModelReader ;
  deer:useSparqlDescribeOf <http://linkedgeodata.org/triplify/node240114473> ;
  deer:fromEndpoint <http://linkedgeodata.org/sparql> ;
.

:node_conf
  a deer:AuthorityConformationEnrichmentOperator ;
  fcage:hasInput :node_reader_lgd ;
  deer:operation [
    deer:sourceAuthority <http://dbpedia.org> ;
    deer:targetAuthority <http://deer.org> ;
  ] ;
.

:node_geofusion
  a deer:GeoFusionEnrichmentOperator ;
  fcage:hasInput ( :node_conf :node_reader_dbp ) ;
  deer:fusionAction "takeAll" ;
  deer:mergeOtherStatements "true"^^xsd:boolean ;
.

:node_filter
  a deer:FilterEnrichmentOperator ;
  fcage:hasInput :node_geofusion ;
  deer:selector [ deer:predicate geo:lat ] ,
            [ deer:predicate geo:long ] ,
            [ deer:predicate rdfs:label ] ,
            [ deer:predicate owl:sameAs ] ;
.

:node_writer
  a deer:FileModelWriter ;
  fcage:hasInput :node_filter ;
  deer:outputFile "output_demo.ttl" ;
  deer:outputFormat "Turtle" ;
.
```
