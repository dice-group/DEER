import React from "react";
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

    this.selectPredicate = this.addWidget(
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

    this.selectPredicate = this.addWidget(
      "text",
      "deer:selectPredicate",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("selectPredicate", v);
        if (document.getElementById("selectPredicate")) {
          document.getElementById("selectPredicate").value = v;
        }
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
        if (document.getElementById("distancePredicate")) {
          document.getElementById("distancePredicate").value = v;
        }
      }
    );

    this.addOutput("output", "text");

    this.title = "GeoDistance Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  submitForm = () => {
    var properties = {
      node: GeoDistanceEnrichmentOperator,
      name: this.state["name"],
      selectPredicate: this.state["selectPredicate"],
      distancePredicate: this.state["distancePredicate"],
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
                //placeholder="Node name"
                onChange={this.handleChange}
                name="name"
                id="name"
              />
            </FormGroup>
            <FormGroup>
              <Label>Select Predicate</Label>
              <Input
                type="text"
                //placeholder="deer:selectPredicate"
                onChange={this.handleChange}
                name="selectPredicate"
                id="selectPredicate"
              />
            </FormGroup>
            <FormGroup>
              <Label>Distance Predicate</Label>
              <Input
                type="text"
                //placeholder="deer:distancePredicate"
                onChange={this.handleChange}
                name="distancePredicate"
                id="nadistancePredicateme"
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

export default GeoDistanceEnrichmentOperator;
