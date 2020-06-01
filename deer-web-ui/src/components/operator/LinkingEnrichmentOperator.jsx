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

    this.addInput("input", "text");
    this.addInput("input", "text");

    this.properties = {
      name: "",
      specFile: "",
      linksPart: "",
      linkSpecification: "",
      linkingPredicate: "",
      threshold: "",
    };

    var that = this;
    var show = true;

    this.specFile = this.addWidget(
      "text",
      "deer:specFile",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("specFile", v);
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
      }
    );

    this.addOutput("output", "text");

    this.title = "Linking Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  toggle = () => {
    this.setState((prevState) => ({
      dropdownOpen: !prevState.dropdownOpen,
    }));
  };

  handleChange = (selectedOption) => {
    this.setState({ selectedOption });
    console.log(`Option selected:`, selectedOption);
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
              <Label>Spec File:</Label>
              <Input type="text" placeholder="deer:specFile" />
            </FormGroup>
            <FormGroup tag="fieldset">
              <legend>Select</legend>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Source
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Target
                </Label>
              </FormGroup>
            </FormGroup>
            <FormGroup>
              <Select
                value={this.state.selectedOption}
                onChange={this.handleChange}
                options={options}
              />
            </FormGroup>
          </Form>
        </CardBody>
        <CardFooter>
          <Button
            className="btn-round"
            color="primary"
            // onClick={this.addNewPrefixes}
          >
            Save
          </Button>
        </CardFooter>
      </Card>
    );
  }
}

export default LinkingEnrichmentOperator;
