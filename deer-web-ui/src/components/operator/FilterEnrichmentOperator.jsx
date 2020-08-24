import React, { Fragment } from "react";
import "litegraph.js/css/litegraph.css";
import Select from "react-select";
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

const options = [
  { value: "subject", label: "Subject" },
  { value: "predicate", label: "Predicate" },
  { value: "object", label: "Object" },
];

class FilterEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      endpoints: [],
      dropdownOpen: true,
      showSelector: false,
      selectedOption: "selector",
    };

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

    this.addInput("input", "text");
    this.properties = {
      name: "",
      showSelector: "",
      selector: "",
      resource: "",
    };

    var that = this;
    var show = true;

    this.name_widget_1 = this.addWidget(
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

    this.combo_widget = this.addWidget(
      "combo",
      "",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("showSelector", v);
        if (document.getElementById("showSelector")) {
          document.getElementById("showSelector").value = v;
        }
      },
      { values: ["Selectors", "Construct Query"] }
    );

    // this.onPropertyChanged = function(name, value) {
    //   console.log(this.widgets[1]);
    //   // if (this.widgets[1].value === "Selectors") {
    //   if (this.widgets[1].value === "Selectors") {
    //     console.log(this.widgets[2]);
    //     this.widgets[2].disabled = true;
    //   }
    //   // }
    // };

    this.selector_combo_widget = this.addWidget(
      "combo",
      "",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("selector", v);
        if (document.getElementById("selector")) {
          document.getElementById("selector").value = v;
        }
      },
      { values: ["subject", "predicate", "object"] }
    );

    this.name_widget = this.addWidget(
      "text",
      "Add resource",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("resource", v);
        if (document.getElementById("resource")) {
          document.getElementById("resource").value = v;
        }
      }
    );

    this.name_widget = this.addWidget(
      "text",
      "Add resource",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("sparqlConstructQuery", v);
        if (document.getElementById("sparqlConstructQuery")) {
          document.getElementById("sparqlConstructQuery").value = v;
        }
      }
    );

    this.addOutput("output", "text");

    this.title = "Filter Enrichment Operator";
    this.color = "#816204";
    this.bgcolor = "#bb8b2c";
  }

  toggle = () => {
    this.setState((prevState) => ({
      dropdownOpen: !prevState.dropdownOpen,
    }));
  };

  submitForm = () => {
    var properties = {
      node: FilterEnrichmentOperator,
      name: this.state["name"],
      selector: this.state["selector"],
      resource: this.state["resource"],
      sparqlConstructQuery: this.state["sparqlConstructQuery"],
    };

    this.props.parentCallback(properties);
  };

  handleSelectChange = (selectedOption) => {
    this.setState({ selectedOption, selector: selectedOption.value });
  };

  handleRadioChange = (event) => {
    this.setState({
      radioOption: event.target.value,
    });

    if (event.target.value === "selector") {
      this.setState({
        showSelector: true,
      });
    } else if (event.target.value === "sparqlConstructQuery") {
      this.setState({
        showSelector: false,
      });
    }
  };

  handleChange = (event) => {
    event.preventDefault();
    let value = event.target.value;
    this.setState({
      [event.target.name]: value,
    });
  };

  render() {
    return (
      <Card className="card-stats">
        <div className="numbers">
          <CardTitle tag="h5">Node Details</CardTitle>
          <p />
        </div>
        <CardBody>
          <Form>
            <FormGroup>
              <Label>Name</Label>
              <Input
                type="text"
                onChange={this.handleChange}
                placeholder="Node name"
                name="name"
                id="name"
              />
            </FormGroup>

            <FormGroup tag="fieldset">
              <legend>Select:</legend>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    name="radio1"
                    value="selector"
                    onChange={this.handleRadioChange}
                    checked={this.state.radioOption === "selector"}
                  />{" "}
                  Selector
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    name="radio1"
                    value="sparqlConstructQuery"
                    onChange={this.handleRadioChange}
                    checked={this.state.radioOption === "sparqlConstructQuery"}
                  />{" "}
                  Construct Query
                </Label>
              </FormGroup>
            </FormGroup>

            {this.state.showSelector ? (
              <div>
                <FormGroup>
                  <Label>Selector</Label>
                  <Select
                    value={this.state.selectedOption}
                    onChange={this.handleSelectChange}
                    options={options}
                    name="selector"
                    id="selector"
                  />
                </FormGroup>
                <FormGroup>
                  <Label>Name</Label>
                  <Input
                    type="text"
                    onChange={this.handleChange}
                    //placeholder="Enter resource"
                    name="resource"
                    id="resource"
                  />
                </FormGroup>
              </div>
            ) : (
              <FormGroup>
                <Label>Use Sparql Construct</Label>
                <Input
                  type="textarea"
                  //placeholder="deer:sparqlConstructQuery"
                  onChange={this.handleChange}
                  name="sparqlConstructQuery"
                  id="sparqlConstructQuery"
                />
              </FormGroup>
            )}
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

export default FilterEnrichmentOperator;
