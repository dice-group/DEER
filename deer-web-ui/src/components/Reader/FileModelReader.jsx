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
  Modal,
  ModalFooter,
  ModalBody,
  ModalHeader,
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
          //this.toggle();
          return;
        }
        that.setProperty("name", v);
      }
    );
    this.addWidget("text", "deer:fromUri", this.properties.name, function (v) {
      if (!v) {
        return;
      }
      that.setProperty("fromUri", v);
    });

    this.name_widget = this.addWidget(
      "text",
      "deer:fromPath",
      this.properties.name,
      function (v) {
        if (!v) {
          return;
        }
        that.setProperty("fromPath", v);
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
      this.setState({
        readerCount: this.state.readerCount + 1,
      });
    };
  }

  toggle = () => {
    console.log("toggle");
    this.setState({
      modal: !this.state.modal,
    });
  };

  handleFromUriChange = (event) => {
    this.setState({
      fromUri: event.target.value,
    });
  };

  handleFromPathChange = (event) => {
    var that = this;
    this.setState({
      fromPath: event.target.value,
    });
    that.setProperty("fromPath", event.target.value);
  };

  submitFormData = (event) => {
    event.preventDefault();
    console.log(this.state.fromUri);
    console.log(this.state.fromPath);
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
                <Label>deer:fromUri</Label>
                <Input
                  type="text"
                  placeholder="fromUri"
                  onChange={this.handleFromUriChange}
                />
              </FormGroup>
              <FormGroup>
                <Label>deer:fromPath</Label>
                <Input
                  type="text"
                  placeholder="fromPath"
                  onChange={this.handleFromPathChange}
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
