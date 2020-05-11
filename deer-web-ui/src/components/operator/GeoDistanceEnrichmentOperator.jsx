import React from "react";
import "litegraph.js/css/litegraph.css";

class GeoDistanceEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      selectPredicate: "",
      distancePredicate: ""
    };

    var that = this;
    var show = true;

    this.selectPredicate = this.addWidget(
      "text",
      "deer:selectPredicate",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("selectPredicate", v);
      }
    );

    this.distancePredicate = this.addWidget(
      "text",
      "deer:distancePredicate",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("distancePredicate", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "GeoDistance Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default GeoDistanceEnrichmentOperator;
