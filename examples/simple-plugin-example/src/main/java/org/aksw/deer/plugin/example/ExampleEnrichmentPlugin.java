package org.aksw.deer.plugin.example;

import org.aksw.deer.enrichments.AbstractEnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExampleEnrichmentPlugin extends Plugin {

    private static final Logger logger = LoggerFactory.getLogger(ExampleEnrichmentPlugin.class);

    public ExampleEnrichmentPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        logger.info("WelcomePlugin.start()");
    }

    @Override
    public void stop() {
        logger.info("WelcomePlugin.stop()");
    }

    @Extension
    public static class ExampleEnrichmentOperator extends AbstractEnrichmentOperator {

        @Override
        protected List<Model> safeApply(List<Model> models) {
            Model model = models.get(0);
            model.setNsPrefix("deer", "http://aksw.org/deer/ontology#");
            Resource resource = model.createResource(model.expandPrefix("deer:examplePlugin"));
            Property property = model.createProperty(model.expandPrefix("deer:says"));
            model.add(resource, property, "Hello World!");
            return List.of(model);
        }
    }
}