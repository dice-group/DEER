import React, { Fragment } from "react";
import "litegraph.js/css/litegraph.css";

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
        if (document.getElementById("name")) {
          document.getElementById("name").value = v;
        }
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
        if (document.getElementById("sourceSubjectAuthority")) {
          document.getElementById("sourceSubjectAuthority").value = v;
        }
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
        if (document.getElementById("targetSubjectAuthority")) {
          document.getElementById("targetSubjectAuthority").value = v;
        }
      }
    );

    this.addOutput("output", "text");

    this.title = "Authority Conformation Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  submitForm = () => {
    var properties = {
      node: AuthorityConformationEnrichmentOperator,
      name: this.state["name"],
      sourceSubjectAuthority: this.state["sourceSubjectAuthority"],
      targetSubjectAuthority: this.state["targetSubjectAuthority"],
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
                placeholder="Node name"
                onChange={this.handleChange}
                name="name"
                id="name"
              />
            </FormGroup>
            <FormGroup>
              <Label>Source Subject Authority</Label>
              <Input
                type="text"
                placeholder="deer:sourceSubjectAuthority"
                onChange={this.handleChange}
                name="sourceSubjectAuthority"
                id="sourceSubjectAuthority"
              />
            </FormGroup>
            <FormGroup>
              <Label>Target Subject Authority</Label>
              <Input
                type="text"
                placeholder="deer:targetSubjectAuthority"
                onChange={this.handleChange}
                name="targetSubjectAuthority"
                id="targetSubjectAuthority"
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

export default AuthorityConformationEnrichmentOperator;
