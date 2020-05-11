import React from "react";
import "litegraph.js/css/litegraph.css";

class LinkingEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true
    };

    this.addInput("input", "text");
    this.addInput("input", "text");

    this.properties = {
      name: "",
      specFile: "",
      linksPart: "",
      linkSpecification: "",
      linkingPredicate: "",
      threshold: ""
    };

    var that = this;
    var show = true;

    this.specFile = this.addWidget(
      "text",
      "deer:specFile",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("specFile", v);
      }
    );

    this.linksPart = this.addWidget(
      "combo",
      "deer:linksPart",
      "Select",
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("linksPart", v);
      },
      { values: ["source", "target"] }
    );

    // this.onPropertyChanged = function(name, value) {
    //   console.log(this.widgets[1]);
    //   // if (this.widgets[1].value === "Selectors") {
    //   if (this.widgets[1].value === "Selectors") {
    //     console.log(this.widgets[2]);
    //     this.widgets[2].disabled = true;
    //   }
    //   // }
    // };

    this.selectMode = this.addWidget(
      "combo",
      "deer:selectMode",
      "Select",
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("selectMode", v);
      },
      { values: ["all", "best1toN", "best1to1", "best"] }
    );

    this.linkSpecification = this.addWidget(
      "text",
      "deer:linkSpecification",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("linkSpecification", v);
      }
    );

    this.linkingPredicate = this.addWidget(
      "text",
      "deer:linkingPredicate",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("linkingPredicate", v);
      }
    );

    this.threshold = this.addWidget(
      "text",
      "deer:threshold",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("threshold", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Linking Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default LinkingEnrichmentOperator;
