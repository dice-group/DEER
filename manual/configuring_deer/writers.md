# Predefined Model Writers

## File Model Writer (`deer:FileModelReader`) {#file}

This Model Writer can write RDF to any
[file format supported by Apache Jena](https://jena.apache.org/documentation/io/rdf-output.html#jena_model_write_formats).  
Its configuration parameters are:

 * `deer:outputFile` *(required)* specifies the name of the emitted file
 * `deer:outputFormat` specifies the file format to be used, defaults to **TURTLE**.
