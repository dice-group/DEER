import React from "react";
import "litegraph.js/css/litegraph.css";

class MergeEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      node_1: "",
      node_2: ""
    };

    var that = this;
    var show = true;

    this.node_1 = this.addWidget(
      "text",
      "deer:node_1",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("node_1", v);
      }
    );
    this.node_2 = this.addWidget(
      "text",
      "deer:node_2",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("node_2", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Merge Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default MergeEnrichmentOperator;
