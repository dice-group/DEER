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
} from "reactstrap";

class FileModelWriter extends React.Component {
  constructor(props) {
    super(props);

    this.addInput("input", "text");
    this.properties = {
      name: "",
      outputFile: "number",
      outputFormat: 0,
    };

    var that = this;

    this.widgets_up = true;
    this.size = [180, 90];

    this.title = "File Model Writer";
    this.color = "#223322";
    this.bgcolor = "#335533";
    this.onDrawForeground = function(ctx, graphcanvas)
    {
      if(this.flags.collapsed)
        return;
      ctx.font = "14px Arial";
      ctx.fillText("Description of the node ...", 10, 40); 
    }
  }

  onExecute() {
    let a = this.getInputData(0);
    if (a === undefined) {
      a = 0;
    }
    //console.log(a);
    this.setOutputData(0, ++a);
  }

  handleChange = (event) => {
    let value = event.target.value;
    this.setState({
      [event.target.name]: value,
    });
  };

  submitForm = () => {
    var properties = {
      node: FileModelWriter,
      name: this.state["name"],
      outputFile: this.state["outputFile"],
      outputFormat: this.state["outputFormat"],
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
                //placeholder="Node name"
                onChange={this.handleChange}
                name="name"
                id="name"
              />
            </FormGroup>
            <FormGroup>
              <Label>Output File</Label>
              <Input
                type="text"
                //placeholder="deer:outputFile"
                onChange={this.handleChange}
                name="outputFile"
                id="outputFile"
              />
            </FormGroup>
            <FormGroup>
              <Label>Output Format</Label>
              <Input
                type="text"
                //placeholder="deer:outputFormat"
                onChange={this.handleChange}
                name="outputFormat"
                id="outputFormat"
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

export default FileModelWriter;
