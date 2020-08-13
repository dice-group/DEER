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

class SparqlModelReader extends React.Component {
  constructor(props) {
    super(props);
    this.properties = {
      name: "",
      fromEndpoint: "",
      sparqlDescribeOf: "",
      useSparqlConstruct: "",
    };

    this.state = {
      endpoints: [],
      showForm: false,
    };
    var that = this;

    fetch("./lod-data.json")
      .then(function (response) {
        return response.json();
      })
      .then((content) => {
        console.log(content);
        let obj = {};
        for (let prop in content) {
          if (content[prop].sparql.length) {
            for (let i = 0; i < content[prop].sparql.length; i++) {
              if (content[prop].sparql[i].status === "OK") {
                obj[content[prop].sparql[i].access_url] = true;
              }
            }
          }
        }
        this.state.endpoints.push(...Object.keys(obj));
      });

    this.name_widget = this.addWidget(
      "text",
      "Reader name",
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
    // this.addWidget(
    //   "combo",
    //   "deer:fromEndpoint",
    //   "Select",
    //   function (v) {
    //     if (!v) {
    //       return;
    //     }
    //     that.setProperty("endpoint", v);
    //   },
    //   { values: this.state.endpoints }
    // );
    this.fromEndpoint = this.addWidget(
      "text",
      "deer:fromEndpoint",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fromEndpoint", v);
        if (document.getElementById("fromEndpoint")) {
          document.getElementById("fromEndpoint").value = v;
        }
      }
    );

    this.sparqlDescribeOf = this.addWidget(
      "text",
      "deer:useSparqlDescribeOf",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sparqlDescribeOf", v);
        if (document.getElementById("sparqlDescribeOf")) {
          document.getElementById("sparqlDescribeOf").value = v;
        }
      }
    );
    this.useSparqlConstruct = this.addWidget(
      "text",
      "deer:useSparqlConstruct",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("useSparqlConstruct", v);
        if (document.getElementById("useSparqlConstruct")) {
          document.getElementById("useSparqlConstruct").value = v;
        }
      }
    );

    this.widgets_up = true;
    this.size = [180, 90];
    this.addOutput("output", "text");
    this.title = "SparQL Model Reader";
    this.color = "#223322";
    this.bgcolor = "#335533";
  }

  handleChange = (event) => {
    let value = event.target.value;
    this.setState({
      [event.target.name]: value,
    });
  };

  submitForm = () => {
    var properties = {
      node: SparqlModelReader,
      name: this.state["name"],
      fromEndpoint: this.state["fromEndpoint"],
      useSparqlDescribeOf: this.state["useSparqlDescribeOf"],
      useSparqlConstruct: this.state["useSparqlConstruct"],
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
                required
              />
            </FormGroup>
            <FormGroup>
              <Label>From endpoint:</Label>
              <Input
                type="text"
                placeholder="deer:fromEndpoint"
                onChange={this.handleChange}
                name="fromEndpoint"
                id="fromEndpoint"
                required
              />
            </FormGroup>
            <FormGroup>
              <Label>Use Sparql Describe of:</Label>
              <Input
                type="text"
                placeholder="deer:useSparqlDescribeOf"
                onChange={this.handleChange}
                name="useSparqlDescribeOf"
                id="useSparqlDescribeOf"
              />
            </FormGroup>
            <FormGroup>
              <Label>Use Sparql Construct</Label>
              <Input
                type="textarea"
                placeholder="deer:useSparqlConstruct"
                onChange={this.handleChange}
                name="useSparqlConstruct"
                id="useSparqlConstruct"
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

export default SparqlModelReader;
