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
  DropdownToggle,
  DropdownMenu,
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
      askEndpoint: false,
    };

    var that = this;
    var show = true;

    this.literalProperty = this.addWidget(
      "text",
      "deer:literalProperty",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("literalProperty", v);
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

    this.neType = this.addWidget(
      "combo",
      "",
      "location",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("neType", v);
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
        that.setProperty("importProperty", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "NER Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  handleChange = (selectedOption) => {
    this.setState({ selectedOption });
    console.log(`Option selected:`, selectedOption);
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
              <Label>Literal Property</Label>
              <Input type="text" placeholder="deer:literalProperty" />
            </FormGroup>
            <FormGroup>
              <Label>Import Property</Label>
              <Select
                value={this.state.selectedOption}
                onChange={this.handleChange}
                options={options}
                placeholder="deer:importProperty"
              />
            </FormGroup>

            <FormGroup tag="fieldset">
              <legend>Select:</legend>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Selector
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Construct Query
                </Label>
              </FormGroup>
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

export default NEREnrichmentOperator;
