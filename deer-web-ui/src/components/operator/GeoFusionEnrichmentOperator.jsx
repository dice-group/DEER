import React from "react";
import "litegraph.js/css/litegraph.css";

class GeoFusionEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      fusionAction: "",
      mergeOtherStatements: "",
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

    this.fusionAction = this.addWidget(
      "combo",
      "",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fusionAction", v);
      },
      { values: ["takeA", "takeB", "takeAll", "takeMostDetailed"] }
    );

    this.mergeOtherStatements = this.addWidget(
      "text",
      "deer:mergeOtherStatements",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("mergeOtherStatements", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "GeoFusion Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default GeoFusionEnrichmentOperator;
