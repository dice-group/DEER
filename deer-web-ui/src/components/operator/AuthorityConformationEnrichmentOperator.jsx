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

class AuthorityConformationEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sourceSubjectAuthority: "",
      targetSubjectAuthority: "",
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

    this.sourceSubjectAuthority = this.addWidget(
      "text",
      "deer:sourceSubjectAuthority",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sourceSubjectAuthority", v);
      }
    );

    this.targetSubjectAuthority = this.addWidget(
      "text",
      "deer:targetSubjectAuthority",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("targetSubjectAuthority", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Authority Conformation Enrichment Operator";
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
              <Label>Source Subject Authority</Label>
              <Input type="text" placeholder="deer:sourceSubjectAuthority" />
            </FormGroup>
            <FormGroup>
              <Label>Target Subject Authority</Label>
              <Input type="text" placeholder="deer:targetSubjectAuthority" />
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

export default AuthorityConformationEnrichmentOperator;
