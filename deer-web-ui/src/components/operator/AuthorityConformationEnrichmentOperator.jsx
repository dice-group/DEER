import React from "react";
import "litegraph.js/css/litegraph.css";

class AuthorityConformationEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sourceAuthority: "",
      targetAuthority: "",
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

    this.sourceAuthority = this.addWidget(
      "text",
      "deer:sourceAuthority",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sourceAuthority", v);
      }
    );

    this.targetAuthority = this.addWidget(
      "text",
      "deer:targetAuthority",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("targetAuthority", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Authority Conformation Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }
}

export default AuthorityConformationEnrichmentOperator;
