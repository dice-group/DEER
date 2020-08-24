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
} from "reactstrap";

class FileModelReader extends React.Component {
  isSelected = false;

  constructor(props) {
    super(props);

    this.properties = {
      name: "",
      fromUri: "",
      fromPath: "",
    };

    this.state = {
      endpoints: [],
      showForm: "",
      readerCount: 0,
      name: "",
      fromUri: "",
      fromPath: "",
      modal: true,
    };

    var that = this;

    this.name_widget = this.addWidget(
      "text",
      "Reader name",
      this.properties.name,
      function (v) {
        if (!v) {
          that.setProperty("name", this.state.name);
          return;
        }
        that.setProperty("name", v);
        if (document.getElementById("readerName")) {
          document.getElementById("readerName").value = v;
        }
      }
    );
    this.fromUriWidget = this.addWidget(
      "text",
      "deer:fromUri",
      this.properties.fromUri,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fromUri", v);
        if (document.getElementById("fromUri")) {
          document.getElementById("fromUri").value = v;
        }
      }
    );

    this.fromPathWidget = this.addWidget(
      "text",
      "deer:fromPath",
      this.properties.fromPath,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fromPath", v);
        if (document.getElementById("fromPath")) {
          document.getElementById("fromPath").value = v;
        }
      }
    );

    this.widgets_up = true;
    this.size = [180, 90];
    this.addOutput("output", "text");
    this.title = "File Model Reader";
    this.color = "#223322";
    this.bgcolor = "#335533";

    this.onNodeAdded = (node) => {
      that.setProperty("name", "reader_" + this.state.readerCount);
      that.setState({
        readerCount: this.state.readerCount + 1,
      });
    };
  }

  handleChange = (event) => {
    this.setState({
      [event.target.name]: event.target.value,
    });

    //this.name_widget.value = event.target.value;
  };

  handleFromUriChange = (event) => {
    this.setState({
      fromUri: event.target.value,
    });
  };
  handleFromPathChange = (event) => {
    this.setState({
      fromPath: event.target.value,
    });
  };

  toggle = () => {
    console.log("toggle");
    this.setState({
      modal: !this.state.modal,
    });
  };

  submitFormData = () => {
    //event.preventDefault();
    var properties = {
      node: FileModelReader,
      name: this.state.name,
      fromUri: this.state.fromUri,
      fromPath: this.state.fromPath,
    };

    this.props.parentCallback(properties);
  };

  render() {
    return (
      <div>
        {/* <Modal
          isOpen={this.state.modal}
          toggle={this.toggle}
          className={this.props.className}
        >
          <ModalHeader toggle={this.toggle}>Modal title</ModalHeader>
          <ModalBody>Please enter a name.</ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={this.toggle}>
              Cancel
            </Button>
          </ModalFooter>
        </Modal> */}

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
                  //placeholder="Node name"
                  onChange={this.handleName}
                  id="readerName"
                  name="name"
                />
              </FormGroup>
              <FormGroup>
                <Label>deer:fromUri</Label>
                <Input
                  type="text"
                  //placeholder="fromUri"
                  onChange={this.handleFromUriChange}
                  id="fromUri"
                  name="fromUri"
                />
              </FormGroup>
              <FormGroup>
                <Label>deer:fromPath</Label>
                <Input
                  type="text"
                  //placeholder="fromPath"
                  onChange={this.handleFromPathChange}
                  id="fromPath"
                  name="fromPath"
                />
              </FormGroup>
            </Form>
          </CardBody>
          <CardFooter>
            <Button
              className="btn-round"
              color="primary"
              onClick={this.submitFormData}
            >
              Save
            </Button>
          </CardFooter>
        </Card>
      </div>
    );
  }
}

export default FileModelReader;
