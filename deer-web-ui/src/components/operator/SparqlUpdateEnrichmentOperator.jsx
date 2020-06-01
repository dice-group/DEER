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
    var show = true;

    this.sparqlUpdateQuery = this.addWidget(
      "text",
      "deer:sparqlUpdateQuery",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sparqlUpdateQuery", v);
      }
    );

    this.addOutput("output", "text");

    this.title = "Sparql Update Enrichment Operator";
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
              <Label>Sparql Update Query</Label>
              <Input
                type="textarea"
                name="text"
                placeholder="deer:sparqlUpdateQuery"
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

export default SparqlUpdateEnrichmentOperator;
