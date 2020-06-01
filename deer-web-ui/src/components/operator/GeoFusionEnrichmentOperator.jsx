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
} from "reactstrap";

const options = [
  { value: "takeA", label: "takeA" },
  { value: "takeB", label: "takeB" },
  { value: "takeAll", label: "takeAll" },
  { value: "takeMostDetailed", label: "takeMostDetailed" },
];
class GeoFusionEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      selectedOption: null,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      fusionAction: "",
      mergeOtherStatements: "",
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

    this.fusionAction = this.addWidget(
      "combo",
      "",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fusionAction", v);
      },
      { values: ["takeA", "takeB", "takeAll", "takeMostDetailed"] }
    );

    this.mergeOtherStatements = this.addWidget(
      "text",
      "deer:mergeOtherStatements",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("mergeOtherStatements", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "GeoFusion Enrichment Operator";
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
          <CardTitle tag="p">Node details</CardTitle>
          <p />
        </div>
        <CardBody>
          <Form>
            <FormGroup>
              <Label>Fusion Action</Label>
              <Select
                value={this.state.selectedOption}
                onChange={this.handleChange}
                options={options}
                placeholder="deer:fusionAction"
              />
            </FormGroup>
            <FormGroup>
              <Label>Merge other statements?</Label>
              <Input type="text" placeholder="deer:mergeOtherStatements" />
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

export default GeoFusionEnrichmentOperator;
