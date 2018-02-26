# Simple Plugin Example

This example plugin provides a simple
"Hello world" enrichment operator as a starting point for your custom plugin development.

DEER uses the [PF4J plugin system](http://pf4j.org).

The code [is self-explanatory](https://github.com/dice-group/deer/blob/master/examples/simple-plugin-example/src/main/java/org/aksw/deer/plugin/example/ExampleEnrichmentPlugin.java).

Using `mvn clean package` in this folder will generate the plugin under
`./target/deer-example-plugin-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.

Try it with the `plugin-demo.ttl` configuration file using
`java -jar path_to_deer_jar plugin-demo.ttl`! 