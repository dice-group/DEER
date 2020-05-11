import React from "react";
import "litegraph.js/css/litegraph.css";

class FilterEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      endpoints: []
    };

    fetch("./lod-data.json")
      .then(function(response) {
        return response.json();
      })
      .then(content => {
        console.log(content);
        let obj = {};
        for (let prop in content) {
          if (content[prop].sparql.length) {
            for (let i = 0; i < content[prop].sparql.length; i++) {
              if (content[prop].sparql[i].status === "OK") {
                obj[content[prop].sparql[i].access_url] = true;
              }
            }
          }
        }
        this.state.endpoints.push(...Object.keys(obj));
      });

    this.addInput("input", "text");
    this.properties = {
      name: "",
      type: "number",
      value: 0
    };

    var that = this;
    var show = true;

    this.name_widget_1 = this.addWidget(
      "text",
      "Operator name",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );

    this.combo_widget = this.addWidget(
      "combo",
      "",
      "Select",
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("showSelector", v);
      },
      { values: ["Selectors", "Construct Query"] }
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

    this.selector_combo_widget = this.addWidget(
      "combo",
      "",
      "Select",
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("selector", v);
      },
      { values: ["subject", "predicate", "object"] }
    );

    this.name_widget = this.addWidget(
      "text",
      "Add resource",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("resource", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Filter Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  // render() {
  //   return <div>hello</div>;
  // }
}

export default FilterEnrichmentOperator;
