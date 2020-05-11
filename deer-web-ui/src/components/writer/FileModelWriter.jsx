import React from "react";
import "litegraph.js/css/litegraph.css";

class FileModelWriter extends React.Component {
  constructor(props) {
    super(props);

    this.addInput("input", "text");
    this.properties = {
      name: "",
      type: "number",
      value: 0
    };

    var that = this;

    this.name_widget = this.addWidget(
      "text",
      "Writer name",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );

    this.name_widget = this.addWidget(
      "text",
      "deer:outputFile",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("outputFile", v);
      }
    );

    this.name_widget = this.addWidget(
      "text",
      "deer:outputFormat",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("outputFormat", v);
      }
    );

    this.widgets_up = true;
    this.size = [180, 90];

    this.title = "File Model Writer";
    this.color = "#223322";
    this.bgcolor = "#335533";
  }

  onExecute() {
    let a = this.getInputData(0);
    if (a === undefined) {
      a = 0;
    }
    //console.log(a);
    this.setOutputData(0, ++a);
  }
}

export default FileModelWriter;
