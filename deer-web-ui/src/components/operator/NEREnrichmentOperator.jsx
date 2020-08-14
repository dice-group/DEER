import React, { Fragment } from "react";
import "litegraph.js/css/litegraph.css";
import Select from "react-select";

// reactstrap components
import {
  Button,
  Form,
  FormGroup,
  Input,
  Label,
  CardBody,
  Card,
  CardTitle,
  CardFooter,
} from "reactstrap";

const options = [
  { value: "location", label: "Location" },
  { value: "person", label: "Person" },
  { value: "organization", label: "Organization" },
  { value: "all", label: "All" },
];

class NEREnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      selectedOption: null,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      literalProperty: "",
      importProperty: "",
      neType: "location",
      foxUrl: false,
    };

    var that = this;

    this.literalProperty = this.addWidget(
      "text",
      "Name",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
        if (document.getElementById("name")) {
          document.getElementById("name").value = v;
        }
      }
    );

    this.literalProperty = this.addWidget(
      "text",
      "deer:literalProperty",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("literalProperty", v);
        if (document.getElementById("literalProperty")) {
          document.getElementById("literalProperty").value = v;
        }
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
        if (document.getElementById("importProperty")) {
          document.getElementById("naimportPropertyme").value = v;
        }
      }
    );

    this.neType = this.addWidget(
      "combo",
      "",
      "location",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("neType", v);
        if (document.getElementById("neType")) {
          document.getElementById("neType").value = v;
        }
      },
      { values: ["location", "person", "organization", "all"] }
    );

    this.askEndpoint = this.addWidget(
      "text",
      "deer:askEndpoint",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("foxUrl", v);
        if (document.getElementById("foxUrl")) {
          document.getElementById("foxUrl").value = v;
        }
      }
    );

    this.addOutput("output", "text");

    this.title = "NER Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  handleSelectChange = (selectedOption) => {
    this.setState({ neType: selectedOption.value });
  };

  submitForm = () => {
    var properties = {
      node: NEREnrichmentOperator,
      name: this.state["name"],
      literalProperty: this.state["literalProperty"],
      importProperty: this.state["importProperty"],
      neType: this.state["neType"],
      foxUrl: this.state["foxUrl"],
    };

    this.props.parentCallback(properties);
  };

  handleChange = (event) => {
    let value = event.target.value;
    this.setState({
      [event.target.name]: value,
    });
  };

  render() {
    return (
      <Card className="card-stats">
        <div className="numbers">
          <CardTitle tag="h5">Node Details</CardTitle>
          <p />
        </div>
        <CardBody>
          <Form>
            <FormGroup>
              <Label>Name</Label>
              <Input
                type="text"
                //placeholder="Node name"
                onChange={this.handleChange}
                name="name"
                id="name"
              />
            </FormGroup>
            <FormGroup>
              <Label>Literal Property</Label>
              <Input
                type="text"
                //placeholder="deer:literalProperty"
                onChange={this.handleChange}
                name="literalProperty"
                id="literalProperty"
              />
            </FormGroup>
            <FormGroup>
              <Label>Import Property</Label>
              <Input
                type="text"
                //placeholder="deer:importProperty"
                onChange={this.handleChange}
                name="importProperty"
                id="importProperty"
              />
            </FormGroup>
            <FormGroup>
              <Label>neType</Label>
              <Select
                value={this.state.selectedOption}
                onChange={this.handleSelectChange}
                options={options}
                //placeholder="deer:neType"
                id="neType"
                name="neType"
              />
            </FormGroup>
            <FormGroup>
              <Label>Endpoint</Label>
              <Input
                type="text"
                //placeholder="deer:foxUrl"
                onChange={this.handleChange}
                name="foxUrl"
                id="foxUrl"
              />
            </FormGroup>
          </Form>
        </CardBody>
        <CardFooter>
          <Button
            className="btn-round"
            color="primary"
            onClick={this.submitForm}
          >
            Save
          </Button>
        </CardFooter>
      </Card>
    );
  }
}

export default NEREnrichmentOperator;
