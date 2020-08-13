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
  Label,
  CardBody,
  Card,
  CardTitle,
  CardFooter,
  DropdownMenu,
  DropdownToggle,
} from "reactstrap";

class SparqlUpdateEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
    };

    this.addInput("input", "text");
    this.properties = {
      name: "",
      sparqlUpdateQuery: "",
    };

    var that = this;

    this.sparqlUpdateQuery = this.addWidget(
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

    this.sparqlUpdateQuery = this.addWidget(
      "text",
      "deer:sparqlUpdateQuery",
      this.properties.sparqlUpdateQuery,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sparqlUpdateQuery", v);
        if (document.getElementById("sparqlUpdateQuery")) {
          document.getElementById("sparqlUpdateQuery").value = v;
        }
      }
    );

    this.addOutput("output", "text");

    this.title = "Sparql Update Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  handleChange = (event) => {
    let value = event.target.value;
    this.setState({
      [event.target.name]: value,
    });
  };

  submitForm = () => {
    var properties = {
      node: SparqlUpdateEnrichmentOperator,
      name: this.state["name"],
      sparqlUpdateQuery: this.state["sparqlUpdateQuery"],
    };

    this.props.parentCallback(properties);
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
              <Label>Sparql Update Query</Label>
              <Input
                type="textarea"
                name="sparqlUpdateQuery"
                id="sparqlUpdateQuery"
                placeholder="deer:sparqlUpdateQuery"
                onChange={this.handleChange}
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

export default SparqlUpdateEnrichmentOperator;
