import React from "react";
import "litegraph.js/css/litegraph.css";

class NEREnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      literalProperty: "",
      importProperty: "",
      neType: "location",
      askEndpoint: false
    };

    var that = this;
    var show = true;

    this.literalProperty = this.addWidget(
      "text",
      "deer:literalProperty",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("literalProperty", v);
      }
    );

    this.importProperty = this.addWidget(
      "text",
      "deer:importProperty",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("importProperty", v);
      }
    );

    this.neType = this.addWidget(
      "combo",
      "",
      "location",
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("neType", v);
      },
      { values: ["location", "person", "organization", "all"] }
    );

    this.askEndpoint = this.addWidget(
      "text",
      "deer:askEndpoint",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("importProperty", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "NER Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default NEREnrichmentOperator;
