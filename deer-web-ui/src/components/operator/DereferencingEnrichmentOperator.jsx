import React from "react";
import "litegraph.js/css/litegraph.css";

class DereferencingEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      lookUpPrefix: "",
      dereferencingProperty: "",
      importProperty: "",
    };

    var that = this;
    var show = true;

    this.name = this.addWidget(
      "text",
      "Operator name",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );

    this.lookUpPrefix = this.addWidget(
      "text",
      "deer:lookUpPrefix",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("lookUpPrefix", v);
      }
    );

    this.dereferencingProperty = this.addWidget(
      "text",
      "deer:dereferencingProperty",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("dereferencingProperty", v);
      }
    );

    this.importProperty = this.addWidget(
      "text",
      "deer:importProperty",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("importProperty", v);
      }
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

    this.addOutput("output", "text");

    this.title = "Dereferencing Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default DereferencingEnrichmentOperator;
