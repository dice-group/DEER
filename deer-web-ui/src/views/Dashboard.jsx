/*!

=========================================================
* Paper Dashboard React - v1.1.0
=========================================================

* Product Page: https://www.creative-tim.com/product/paper-dashboard-react
* Copyright 2019 Creative Tim (https://www.creative-tim.com)

* Licensed under MIT (https://github.com/creativetimofficial/paper-dashboard-react/blob/master/LICENSE.md)

* Coded by Creative Tim

=========================================================

* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

*/
import React, { Fragment } from "react";
// react plugin used to create charts
import { LGraph, LGraphCanvas, LiteGraph } from "litegraph.js";
import _ from "lodash";
import "litegraph.js/css/litegraph.css";
import "./Dashboard.css";

import FileModelReader from "../components/Reader/FileModelReader";
import FileModelWriter from "../components/writer/FileModelWriter";
import SparQLModelReader from "../components/Reader/SparQLModelReader";
import FilterEnrichmentOperator from "../components/operator/FilterEnrichmentOperator";
import LinkingEnrichmentOperator from "../components/operator/LinkingEnrichmentOperator";
import DereferencingEnrichmentOperator from "../components/operator/DereferencingEnrichmentOperator";
import NEREnrichmentOperator from "../components/operator/NEREnrichmentOperator";
import CloneEnrichmentOperator from "../components/operator/CloneEnrichmentOperator";
import MergeEnrichmentOperator from "../components/operator/MergeEnrichmentOperator";
import GeoFusionEnrichmentOperator from "../components/operator/GeoFusionEnrichmentOperator";
import SparqlUpdateEnrichmentOperator from "../components/operator/SparqlUpdateEnrichmentOperator";
import GeoDistanceEnrichmentOperator from "../components/operator/GeoDistanceEnrichmentOperator";
import AuthorityConformationEnrichmentOperator from "../components/operator/AuthorityConformationEnrichmentOperator";
import PredicateConformationEnrichmentOperator from "../components/operator/PredicateConformationEnrichmentOperator";

// reactstrap components
import {
  Card,
  CardHeader,
  CardBody,
  CardFooter,
  CardTitle,
  Row,
  Col,
  Button,
  ModalHeader,
  Modal,
  ModalBody,
  Form,
  FormGroup,
  Input,
  Dropdown,
  DropdownItem,
  Badge,
} from "reactstrap";
//import { Dropdown } from "semantic-ui-react";

const N3 = require("n3");
const { DataFactory } = N3;
const { namedNode, literal, defaultGraph } = DataFactory;

class Dashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      graph: new LGraph(),
      outputLinks: [],
      prefixOptions: [],
      config: "",
      isModalOpen: false,
      namespace: "",
      userInput: "",
      afterFilteredSuggestions: [],
      addNewPrefixes: [],
      showForm: false,
      showComponent: "",
      node: "",
      //TODO: will be updated
      componentArray: [
        {
          src: <FileModelReader />,
          title: "File Model Reader",
        },
        {
          src: <FileModelWriter />,
          title: "File Model Writer",
        },
        {
          src: <FilterEnrichmentOperator />,
          title: "Filter Enrichment Operator",
        },
        {
          src: <LinkingEnrichmentOperator />,
          title: "Linking Enrichment Operator",
        },
        {
          src: <NEREnrichmentOperator />,
          title: "NER Enrichment Operator",
        },
        {
          src: <GeoFusionEnrichmentOperator />,
          title: "GeoFusion Enrichment Operator",
        },
        {
          src: <AuthorityConformationEnrichmentOperator />,
          title: "Authority Conformation Enrichment Operator",
        },
        {
          src: <PredicateConformationEnrichmentOperator />,
          title: "Predicate Conformation Enrichment Operator",
        },
        {
          src: <GeoDistanceEnrichmentOperator />,
          title: "GeoDistance Enrichment Operator",
        },
        {
          src: <SparqlUpdateEnrichmentOperator />,
          title: "Sparql Update Enrichment Operator",
        },
      ],
    };
  }

  componentDidMount() {
    var graphCanvas = new LGraphCanvas("#mycanvas", this.state.graph);
    this.state.graph.start();

    graphCanvas.onShowNodePanel = (node) => {
      this.setState({
        node: node,
      });
    };

    fetch("https://prefix.cc/context")
      .then(function (response) {
        return response.json();
      })
      .then((content) => {
        this.setState({
          contexts: content["@context"],
          prefixOptions: Object.keys(content["@context"]),
        });
      });
  }

  toggleDropdown = () => {
    this.setState({
      show: !this.state.show,
    });
  };

  getOutputLinks = (node) => {
    for (let link in node.outputs) {
      if (node.outputs[link].links) {
        for (let nodeLink in node.outputs[link].links) {
          var link_id = node.outputs[link].links[nodeLink];
          var linkInGraph = this.state.graph.links[link_id];
          if (linkInGraph) {
            var target_node = this.state.graph.getNodeById(
              linkInGraph.target_id
            );
            return target_node;
          }
        }
      }
    }
  };

  getInputLink = (node) => {
    for (let link in node.inputs) {
      if (node.inputs[link].link) {
        var inputLinkId = node.inputs[link].link;
        var inputLinkInGraph = this.state.graph.links[inputLinkId];
        if (inputLinkInGraph) {
          var inputOriginNode = this.state.graph.getNodeById(
            inputLinkInGraph.origin_id
          );
          return inputOriginNode;
        }
      }
    }
  };

  saveConfig = () => {
    var data = this.state.graph.serialize();
    this.setState({
      isModalOpen: true,
    });
    //use N3
    const myQuad = DataFactory.quad(
      namedNode("https://ruben.verborgh.org/profile/#me"),
      namedNode("http://xmlns.com/foaf/0.1/givenName"),
      literal("Ruben", "en"),
      defaultGraph()
    );

    //can be changed
    var prefixes = {
      example: "urn:example:demo/",
      foaf: "http://xmlns.com/foaf/0.1/",
      dbpedia: "http://dbpedia.org/resource/",
      deer: "http://w3id.org/deer/",
      fcage: "http://w3id.org/fcage/",
      geo: "http://www.w3.org/2003/01/geo/wgs84_pos#",
      owl: "http://www.w3.org/2002/07/owl#",
      rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      rdfs: "http://www.w3.org/2000/01/rdf-schema#",
      xsd: "http://www.w3.org/2001/XMLSchema#",
    };

    var writer = new N3.Writer(
      {
        prefixes: prefixes,
      },
      { format: "N-Triples" }
    );

    var parser = new N3.Parser({ format: "N3", blankNodePrefix: "" });

    data.nodes.map((node, key) => {
      // console.log(node);
      writer.addQuad(
        namedNode("urn:example:demo/" + node.properties.name),
        namedNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), //predicate
        namedNode("http://w3id.org/deer/" + node.type.split("/")[1]) //object
      );
      if (node.type === "Reader/FileModelReader") {
        if (node.properties.fromUri) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "fromUri"),
            namedNode(node.properties.fromUri)
          );
        }
      }
      if (node.type === "Reader/SparQLModelReader") {
        console.log(node.properties.endpoint);
      }

      if (node.type === "Writer/FileModelWriter") {
        if (node.properties.outputFile && node.properties.outputFormat) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "outputFile"),
            literal(node.properties.outputFile)
          );

          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "outputFormat"),
            literal(node.properties.outputFormat)
          );
        }
      }
      //Filter Enrichment Operator
      if (node.type === "Operator/FilterEnrichmentOperator") {
        if (
          node.properties.showSelector &&
          node.properties.showSelector === "Selectors"
        ) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "selector"), //predicate
            writer.blank([
              {
                predicate: namedNode(
                  "http://w3id.org/deer/" + node.properties.selector
                ),
                object: namedNode(
                  "http://w3id.org/deer/" + node.properties.resource
                ),
              },
            ])
          );
        }
      }
      //Dereferencing enrichment Operator
      if (node.type === "Operator/DereferencingEnrichmentOperator") {
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/operation"),
          writer.blank([
            {
              predicate: namedNode("urn:example:demo/" + "lookUpPrefix"),
              object: literal(node.properties.lookUpPrefix),
            },
            {
              predicate: namedNode(
                "urn:example:demo/" + "dereferencingProperty"
              ),
              object: literal(node.properties.dereferencingProperty),
            },
            {
              predicate: namedNode("urn:example:demo/" + "importProperty"),
              object: literal(node.properties.importProperty),
            },
          ])
        );
      }

      //GeoFusion Enrichment Operator
      if (node.type === "Operator/GeoFusionEnrichmentOperator") {
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/fusionAction"),
          literal(node.properties.fusionAction)
        );

        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/mergeOtherStatements"),
          literal(node.properties.mergeOtherStatements)
        );
      }

      //Authority Conformation Enrichment Operator
      if (node.type === "Operator/AuthorityConformationEnrichmentOperator") {
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/operation"),
          writer.blank([
            {
              predicate: namedNode(
                "http://w3id.org/deer/sourceSubjectAuthority"
              ),
              object: namedNode(node.properties.sourceSubjectAuthority),
            },
            {
              predicate: namedNode(
                "http://w3id.org/deer/targetSubjectAuthority"
              ),
              object: namedNode(node.properties.targetSubjectAuthority),
            },
          ])
        );
      }

      //Predicate Conformation Enrichment Operator
      if (node.type === "Operator/PredicateConformationEnrichmentOperator") {
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/operation"),
          writer.blank([
            {
              predicate: namedNode("http://w3id.org/deer/sourcePredicate"),
              object: literal(node.properties.sourcePredicate),
            },
            {
              predicate: namedNode("http://w3id.org/deer/targetPredicate"),
              object: literal(node.properties.targetPredicate),
            },
          ])
        );
      }

      //GeoDistance Enrichment Operator
      if (node.type === "Operator/GeoDistanceEnrichmentOperator") {
      }

      if (this.getOutputLinks(node)) {
        //if it has output links, check and add a quad
        var readerTargetNode = this.getOutputLinks(node);
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/fcage/" + "hasOutput"),
          namedNode("urn:example:demo/" + readerTargetNode.properties.name)
        );
      }
      //if it has an input link, check and add a quad
      if (this.getInputLink(node)) {
        var originInputNode = this.getInputLink(node);
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/fcage/" + "hasInput"),
          namedNode("urn:example:demo/" + originInputNode.properties.name)
        );
      }
    });
    writer.end((error, result) => {
      console.log(result);
      this.setState({
        config: result,
      });
    });
  };

  toggle = () => {
    this.setState({
      isModalOpen: !this.state.isModalOpen,
    });
  };

  getFilteredOptions = (event) => {
    this.setState({
      afterFilteredSuggestions: this.state.prefixOptions.filter((i) => {
        return i.toLowerCase().includes(event.target.value.toLowerCase());
      }),
      userInput: event.target.value,
    });
  };

  selectedPrefix = (event) => {
    this.setState({
      afterFilteredSuggestions: [],
      namespace: this.state.contexts[event.target.value],
      userInput: event.target.value,
    });
  };

  handleNamespaceChange = (event) => {
    this.setState({
      namespace: event.target.value,
    });
  };

  addNewPrefixes = (e) => {
    this.setState({
      addNewPrefixes: this.state.addNewPrefixes.concat(this.state.userInput),
    });
  };

  render() {
    const options = _.map(this.state.prefixOptions, (opt, index) => ({
      key: opt,
      text: opt,
      value: opt,
    }));
    return (
      <div className="content">
        <Row>
          <Col lg="12" md="12" sm="12">
            <Card className="card-stats">
              <div className="numbers">
                <CardTitle tag="p">Prefixes</CardTitle>
                <p />
              </div>
              <CardBody>
                <Row>
                  <Col md="12" xs="12">
                    <Form>
                      <Row>
                        <Col className="pr-1" md="4">
                          <Fragment>
                            <FormGroup className="dropdown">
                              <label>Label</label>
                              <Input
                                placeholder="Label"
                                type="text"
                                value={this.state.userInput}
                                onChange={this.getFilteredOptions}
                              ></Input>
                              <Dropdown
                                className="dropdown-content"
                                toggle={this.toggleDropdown}
                              >
                                {this.state.afterFilteredSuggestions.map(
                                  (suggestions, key) => (
                                    <DropdownItem
                                      className="dropdown-item"
                                      value={suggestions}
                                      onClick={this.selectedPrefix}
                                    >
                                      {suggestions}
                                    </DropdownItem>
                                  )
                                )}
                              </Dropdown>
                            </FormGroup>
                          </Fragment>
                        </Col>
                        <Col className="pl-1" md="4">
                          <FormGroup>
                            <label>Namespace</label>
                            <Input
                              placeholder="Namespace"
                              type="text"
                              value={this.state.namespace}
                              onChange={this.handleNamespaceChange}
                            />
                          </FormGroup>
                        </Col>
                        <div className="pl-1" md="4">
                          <Button
                            className="btn-round prefixBtn"
                            color="primary"
                            onClick={this.addNewPrefixes}
                          >
                            Add prefix
                          </Button>
                        </div>
                      </Row>
                    </Form>
                  </Col>
                </Row>
              </CardBody>
              <CardFooter>
                <hr />
                <div className="stats">
                  {this.state.addNewPrefixes.map((addPrefix) => (
                    <Badge variant="light"> {addPrefix}</Badge>
                  ))}
                </div>
              </CardFooter>
            </Card>
          </Col>
        </Row>
        <Row>
          <Col md="9">
            <Card>
              <CardHeader>
                <CardTitle tag="h5">Graph</CardTitle>
              </CardHeader>
              <CardBody>
                <canvas id="mycanvas" width="800" height="600"></canvas>{" "}
              </CardBody>
              <CardFooter>
                <hr />
                <div className="stats">
                  <Button onClick={this.saveConfig}>
                    <i
                      className="fa fa-sticky-note"
                      style={{ color: `white` }}
                    />{" "}
                    Save Configuration
                  </Button>
                </div>
              </CardFooter>
            </Card>
          </Col>
          <Modal isOpen={this.state.isModalOpen} toggle={this.toggle}>
            <ModalHeader toggle={this.toggle}>Display Config</ModalHeader>
            <ModalBody>
              <h1>{this.state.config}</h1>
            </ModalBody>
          </Modal>
          <Col md="3">
            {this.state.componentArray.map((comp, key) => {
              if (this.state.node.title === comp.title) {
                return comp.src;
              }
            })}
          </Col>
        </Row>
        {/* <Row>
            <Col md="4">
              <Card>
                <CardHeader>
                  <CardTitle tag="h5">Email Statistics</CardTitle>
                  <p className="card-category">Last Campaign Performance</p>
                </CardHeader>
                <CardBody>
                  <Pie
                    data={dashboardEmailStatisticsChart.data}
                    options={dashboardEmailStatisticsChart.options}
                  />
                </CardBody>
                <CardFooter>
                  <div className="legend">
                    <i className="fa fa-circle text-primary" /> Opened{" "}
                    <i className="fa fa-circle text-warning" /> Read{" "}
                    <i className="fa fa-circle text-danger" /> Deleted{" "}
                    <i className="fa fa-circle text-gray" /> Unopened
                  </div>
                  <hr />
                  <div className="stats">
                    <i className="fa fa-calendar" /> Number of emails sent
                  </div>
                </CardFooter>
              </Card>
            </Col>
            <Col md="8">
              <Card className="card-chart">
                <CardHeader>
                  <CardTitle tag="h5">NASDAQ: AAPL</CardTitle>
                  <p className="card-category">Line Chart with Points</p>
                </CardHeader>
                <CardBody>
                  <Line
                    data={dashboardNASDAQChart.data}
                    options={dashboardNASDAQChart.options}
                    width={400}
                    height={100}
                  />
                </CardBody>
                <CardFooter>
                  <div className="chart-legend">
                    <i className="fa fa-circle text-info" /> Tesla Model S{" "}
                    <i className="fa fa-circle text-warning" /> BMW 5 Series
                  </div>
                  <hr />
                  <div className="card-stats">
                    <i className="fa fa-check" /> Data information certified
                  </div>
                </CardFooter>
              </Card>
            </Col>
          </Row> */}
      </div>
    );
  }
}

LiteGraph.registered_node_types = {};
LiteGraph.searchbox_extras = {};
LiteGraph.registerNodeType("Reader/FileModelReader", FileModelReader);
LiteGraph.registerNodeType("Reader/SparQLModelReader", SparQLModelReader);
LiteGraph.registerNodeType("Writer/FileModelWriter", FileModelWriter);
LiteGraph.registerNodeType(
  "Operator/FilterEnrichmentOperator",
  FilterEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/LinkingEnrichmentOperator",
  LinkingEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/DereferencingEnrichmentOperator",
  DereferencingEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/NEREnrichmentOperator",
  NEREnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/CloneEnrichmentOperator",
  CloneEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/MergeEnrichmentOperator",
  MergeEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/GeoFusionEnrichmentOperator",
  GeoFusionEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/AuthorityConformationEnrichmentOperator",
  AuthorityConformationEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/PredicateConformationEnrichmentOperator",
  PredicateConformationEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/GeoDistanceEnrichmentOperator",
  GeoDistanceEnrichmentOperator
);
LiteGraph.registerNodeType(
  "Operator/SparqlUpdateEnrichmentOperator",
  SparqlUpdateEnrichmentOperator
);

export default Dashboard;
