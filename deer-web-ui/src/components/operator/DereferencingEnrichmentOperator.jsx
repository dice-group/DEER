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

class DereferencingEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      lookUpPrefix: "",
      dereferencingProperty: "",
      importProperty: "",
    };

    var that = this;
    var show = true;

    this.name = this.addWidget(
      "text",
      "Operator name",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("name", v);
      }
    );

    this.lookUpPrefix = this.addWidget(
      "text",
      "deer:lookUpPrefix",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("lookUpPrefix", v);
      }
    );

    this.dereferencingProperty = this.addWidget(
      "text",
      "deer:dereferencingProperty",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("dereferencingProperty", v);
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

    this.addOutput("output", "text");

    this.title = "Dereferencing Enrichment Operator";
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
              <Label>Look Up Prefix</Label>
              <Input
                type="text"
                //placeholder="deer:lookUpPrefix"
              />
            </FormGroup>
            <FormGroup>
              <Label>Dereferencing Property</Label>
              <Input
                type="text"
                //placeholder="deer:dereferencingProperty"
              />
            </FormGroup>
            <FormGroup>
              <Label>Import Property</Label>
              <Input
                type="text"
                //placeholder="deer:importProperty"
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

export default DereferencingEnrichmentOperator;
