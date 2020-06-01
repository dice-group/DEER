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

class PredicateConformationEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sourcePredicate: "",
      targetPredicate: "",
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

    this.sourcePredicate = this.addWidget(
      "text",
      "deer:sourcePredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sourcePredicate", v);
      }
    );

    this.targetPredicate = this.addWidget(
      "text",
      "deer:targetPredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("targetPredicate", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Predicate Conformation Enrichment Operator";
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
              <Label>Source Predicate</Label>
              <Input type="text" placeholder="deer:sourcePredicate" />
            </FormGroup>
            <FormGroup>
              <Label>Target Predicate</Label>
              <Input type="text" placeholder="deer:targetPredicate" />
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

export default PredicateConformationEnrichmentOperator;
