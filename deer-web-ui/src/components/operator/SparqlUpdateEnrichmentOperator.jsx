import React from "react";
import "litegraph.js/css/litegraph.css";

class SparqlUpdateEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sparqlUpdateQuery: ""
    };

    var that = this;
    var show = true;

    this.sparqlUpdateQuery = this.addWidget(
      "text",
      "deer:sparqlUpdateQuery",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("sparqlUpdateQuery", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Sparql Update Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default SparqlUpdateEnrichmentOperator;
