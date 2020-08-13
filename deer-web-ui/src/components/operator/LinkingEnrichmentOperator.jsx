import React, { Fragment } from "react";
import "litegraph.js/css/litegraph.css";
import Select from "react-select";

// reactstrap components
import {
  Row,
  Col,
  Button,
  Form,
  FormGroup,
  Input,
  Dropdown,
  DropdownItem,
  Label,
  CardBody,
  Card,
  CardTitle,
  CardFooter,
  DropdownMenu,
  DropdownToggle,
} from "reactstrap";

const options = [
  { value: "all", label: "all" },
  { value: "best1toN", label: "best1toN" },
  { value: "best1to1", label: "best1to1" },
  { value: "best", label: "best" },
];

class LinkingEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      selectedOption: null,
    };

    this.addInput("input1", "text");
    this.addInput("input2", "text");

    this.properties = {
      name: "",
      specFile: "",
      linksPart: "",
      linkSpecification: "",
      linkingPredicate: "",
      threshold: "",
    };

    var that = this;

    this.name_widget = this.addWidget(
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

    this.specFile = this.addWidget(
      "text",
      "deer:specFile",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("specFile", v);
        if (document.getElementById("specFile")) {
          document.getElementById("specFile").value = v;
        }
      }
    );

    this.linksPart = this.addWidget(
      "combo",
      "deer:linksPart",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("linksPart", v);
        if (document.getElementById("linksPart")) {
          document.getElementById("linksPart").value = v;
        }
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
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("selectMode", v);
        if (document.getElementById("selectMode")) {
          document.getElementById("selectMode").value = v;
        }
      },
      { values: ["all", "best1toN", "best1to1", "best"] }
    );

    this.linkSpecification = this.addWidget(
      "text",
      "deer:linkSpecification",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("linkSpecification", v);
        if (document.getElementById("linkSpecification")) {
          document.getElementById("linkSpecification").value = v;
        }
      }
    );

    this.linkingPredicate = this.addWidget(
      "text",
      "deer:linkingPredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("linkingPredicate", v);
        if (document.getElementById("linkingPredicate")) {
          document.getElementById("linkingPredicate").value = v;
        }
      }
    );

    this.threshold = this.addWidget(
      "text",
      "deer:threshold",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("threshold", v);
        if (document.getElementById("threshold")) {
          document.getElementById("threshold").value = v;
        }
      }
    );

    this.addOutput("output1", "text");
    this.addOutput("output2", "text");
    this.addOutput("output3", "text");

    this.title = "Linking Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  toggle = () => {
    this.setState((prevState) => ({
      dropdownOpen: !prevState.dropdownOpen,
    }));
  };

  handleSelectChange = (selectedOption) => {
    this.setState({ selectedOption, selectMode: selectedOption.value });
  };

  submitForm = () => {
    var properties = {
      node: LinkingEnrichmentOperator,
      name: this.state["name"],
      specFile: this.state["specFile"],
      linksPart: this.state["linksPart"],
      selectMode: this.state["selectMode"],
      linkSpecification: this.state["linkSpecification"],
      linkingPredicate: this.state["linkingPredicate"],
      threshold: this.state["threshold"],
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
          <CardTitle tag="p">Node details</CardTitle>
          <p />
        </div>
        <CardBody>
          <Form>
            <FormGroup>
              <Label>Name</Label>
              <Input
                type="text"
                onChange={this.handleChange}
                placeholder="Node name"
                name="name"
                id="name"
              />
            </FormGroup>
            <FormGroup>
              <Label>Spec File:</Label>
              <Input
                type="text"
                placeholder="deer:specFile"
                onChange={this.handleChange}
                name="specFile"
                id="specFile"
              />
            </FormGroup>
            <FormGroup tag="fieldset">
              <legend>LinksPart</legend>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    name="radio1"
                    value="Source"
                    onChange={this.handleChange}
                  />{" "}
                  Source
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    name="radio1"
                    value="Target"
                    onChange={this.handleChange}
                  />{" "}
                  Target
                </Label>
              </FormGroup>
            </FormGroup>
            <FormGroup>
              <Label>Select Mode</Label>
              <Select
                value={this.state.selectedOption}
                onChange={this.handleSelectChange}
                options={options}
                name="selectMode"
                id="selectMode"
              />
            </FormGroup>
            <FormGroup>
              <Label>Link Specification:</Label>
              <Input
                type="text"
                placeholder="deer:linkSpecification"
                onChange={this.handleChange}
                name="linkSpecification"
                id="linkSpecification"
              />
            </FormGroup>
            <FormGroup>
              <Label>Linking Predicate:</Label>
              <Input
                type="text"
                placeholder="deer:linkingPredicate"
                onChange={this.handleChange}
                name="linkingPredicate"
                id="linkingPredicate"
              />
            </FormGroup>
            <FormGroup>
              <Label>Threshold:</Label>
              <Input
                type="text"
                placeholder="deer:threshold"
                onChange={this.handleChange}
                name="threshold"
                id="threshold"
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

export default LinkingEnrichmentOperator;
