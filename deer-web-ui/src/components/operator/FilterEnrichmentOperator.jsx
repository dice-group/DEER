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
  DropdownToggle,
  DropdownMenu,
  Label,
  CardBody,
  Card,
  CardTitle,
  CardFooter,
} from "reactstrap";

class FilterEnrichmentOperator extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSelectors: true,
      endpoints: [],
      dropdownOpen: true,
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
      showSelector: "number",
      selector: "",
      resource: "",
    };

    var that = this;
    var show = true;

    this.name_widget_1 = this.addWidget(
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

    this.combo_widget = this.addWidget(
      "combo",
      "",
      "Select",
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("showSelector", v);
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

  render() {
    return (
      <Card className="card-stats">
        <div className="numbers">
          <CardTitle tag="h5">Node Details</CardTitle>
          <p />
        </div>
        <CardBody>
          <Form>
            <FormGroup tag="fieldset">
              <legend>Select:</legend>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Selector
                </Label>
              </FormGroup>
              <FormGroup check>
                <Label check>
                  <Input type="radio" name="radio1" /> Construct Query
                </Label>
              </FormGroup>
            </FormGroup>

            <FormGroup>
              <Dropdown isOpen={this.state.dropdownOpen} toggle={this.toggle}>
                <DropdownToggle caret>Select</DropdownToggle>
                <DropdownMenu>
                  <DropdownItem>Subject</DropdownItem>
                  <DropdownItem>Predicate</DropdownItem>
                  <DropdownItem>Object</DropdownItem>
                </DropdownMenu>
              </Dropdown>
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

export default FilterEnrichmentOperator;
