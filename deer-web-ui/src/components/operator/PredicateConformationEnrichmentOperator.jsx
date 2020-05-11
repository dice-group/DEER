import React from "react";
import "litegraph.js/css/litegraph.css";

class PredicateConformationEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sourcePredicate: "",
      targetPredicate: "",
    };

    var that = this;
    var show = true;

    this.name = this.addWidget(
      "text",
      "Operator Name",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );

    this.sourcePredicate = this.addWidget(
      "text",
      "deer:sourcePredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sourcePredicate", v);
      }
    );

    this.targetPredicate = this.addWidget(
      "text",
      "deer:targetPredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("targetPredicate", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Predicate Conformation Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default PredicateConformationEnrichmentOperator;
