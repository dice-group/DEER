import React, { Fragment } from "react";
import "litegraph.js/css/litegraph.css";

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

class GeoDistanceEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      selectPredicate: "",
      distancePredicate: "",
    };

    var that = this;
    var show = true;

    this.selectPredicate = this.addWidget(
      "text",
      "deer:selectPredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("selectPredicate", v);
      }
    );

    this.distancePredicate = this.addWidget(
      "text",
      "deer:distancePredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("distancePredicate", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "GeoDistance Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

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
              <Label>Select Predicate</Label>
              <Input type="text" placeholder="deer:selectPredicate" />
            </FormGroup>
            <FormGroup>
              <Label>Distance Predicate</Label>
              <Input type="text" placeholder="deer:distancePredicate" />
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

export default GeoDistanceEnrichmentOperator;
