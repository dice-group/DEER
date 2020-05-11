import React from "react";
import "litegraph.js/css/litegraph.css";

class FileModelReader extends React.Component {
  isSelected = false;
  state = {
    endpoints: [],
    showForm: ""
  };
  constructor(props) {
    super(props);

    this.properties = {
      name: "",
      type: "number",
      value: 0,
      selected: ""
    };

    var that = this;
    this.onSelected = this.onSelected.bind(that);
    this.onDeselected = this.onDeselected.bind(that);

    this.name_widget = this.addWidget(
      "text",
      "Reader name",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );
    this.addWidget("text", "deer:fromUri", this.properties.name, function(v) {
      if (!v) {
        return;
      }
      that.setProperty("fromUri", v);
    });

    this.name_widget = this.addWidget(
      "text",
      "deer:fromPath",
      this.properties.name,
      function(v) {
        if (!v) {
          return;
        }
        that.setProperty("fromPath", v);
      }
    );

    this.widgets_up = true;
    this.size = [180, 90];
    this.addOutput("output", "text");
    this.title = "File Model Reader";
    this.color = "#223322";
    this.bgcolor = "#335533";
  }

  componentDidUpdate(isSelected) {
    console.log(isSelected);
    // console.log(this.selected);
    if (this.isSelected !== isSelected) {
      this.isSelected = isSelected;
    }
  }

  onSelected = () => {
    this.isSelected = true;
    this.render();
    this.setProperty("selected", false);
  };

  onDeselected = () => {
    this.isSelected = false;
    this.render();
    this.setProperty("selected", true);
  };

  render() {
    return <div></div>;
  }
}

export default FileModelReader;
