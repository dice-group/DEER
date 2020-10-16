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

import FileModelReader from "../components/Readers/FileModelReader";
import FileModelWriter from "../components/Writers/FileModelWriter";
import SparqlModelReader from "../components/Readers/SparqlModelReader";
import FilterEnrichmentOperator from "../components/Operators/FilterEnrichmentOperator";
import LinkingEnrichmentOperator from "../components/Operators/LinkingEnrichmentOperator";
import DereferencingEnrichmentOperator from "../components/Operators/DereferencingEnrichmentOperator";
import NEREnrichmentOperator from "../components/Operators/NEREnrichmentOperator";
import CloneEnrichmentOperator from "../components/Operators/CloneEnrichmentOperator";
import MergeEnrichmentOperator from "../components/Operators/MergeEnrichmentOperator";
import GeoFusionEnrichmentOperator from "../components/Operators/GeoFusionEnrichmentOperator";
import SparqlUpdateEnrichmentOperator from "../components/Operators/SparqlUpdateEnrichmentOperator";
import GeoDistanceEnrichmentOperator from "../components/Operators/GeoDistanceEnrichmentOperator";
import AuthorityConformationEnrichmentOperator from "../components/Operators/AuthorityConformationEnrichmentOperator";
import PredicateConformationEnrichmentOperator from "../components/Operators/PredicateConformationEnrichmentOperator";

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
  Table,
} from "reactstrap";
//import { Dropdown } from "semantic-ui-react";

const N3 = require("n3");
const { DataFactory } = N3;
const { namedNode, literal, defaultGraph } = DataFactory;
const URI = "http://localhost:8080";
const BASE_URI = window.location.hostname + ":" + window.location.port;

class Dashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      graph: new LGraph(),
      outputLinks: [],
      prefixOptions: [],
      config: ``,
      isModalOpen: false,
      namespace: "",
      userInput: "",
      afterFilteredSuggestions: [],
      addNewPrefixes: [],
      showForm: false,
      showComponent: "",
      node: "",
      isDisabled: false,
      nodeArr: [],
      file: "",
      visible: false,
      requestID: "",
      showResultModal: false,
      requestCompleteModal: false,
      showConfigButton: false,
      availableFiles: [],
      showLogButton: false,
      formProperties: "",
      input1: "",
      input2: "",
      prefixes: {
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
      },
      componentArray: [
        {
          src: FileModelReader,
          title: "File Model Reader",
          name: "FileModelReader",
          url: "Reader/FileModelReader",
          form: <FileModelReader parentCallback={this.callbackFunction} />,
        },
        {
          src: FileModelWriter,
          title: "File Model Writer",
          name: "FileModelWriter",
          url: "Writer/FileModelWriter",
          form: <FileModelWriter parentCallback={this.callbackFunction} />,
        },
        {
          src: FilterEnrichmentOperator,
          title: "Filter Enrichment Operator",
          name: "FilterEnrichmentOperator",
          url: "Operator/FilterEnrichmentOperator",
          form: (
            <FilterEnrichmentOperator parentCallback={this.callbackFunction} />
          ),
        },
        {
          src: LinkingEnrichmentOperator,
          title: "Linking Enrichment Operator",
          name: "LinkingEnrichmentOperator",
          url: "Operator/LinkingEnrichmentOperator",
          form: (
            <LinkingEnrichmentOperator parentCallback={this.callbackFunction} />
          ),
        },
        {
          src: NEREnrichmentOperator,
          title: "NER Enrichment Operator",
          name: "NEREnrichmentOperator",
          url: "Operator/NEREnrichmentOperator",
          form: (
            <NEREnrichmentOperator parentCallback={this.callbackFunction} />
          ),
        },
        {
          src: DereferencingEnrichmentOperator,
          title: "Dereferencing Enrichment Operator",
          name: "DereferencingEnrichmentOperator",
          url: "Operator/DereferencingEnrichmentOperator",
          form: (
            <DereferencingEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: GeoFusionEnrichmentOperator,
          title: "GeoFusion Enrichment Operator",
          name: "GeoFusionEnrichmentOperator",
          url: "Operator/GeoFusionEnrichmentOperator",
          form: (
            <GeoFusionEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: AuthorityConformationEnrichmentOperator,
          title: "Authority Conformation Enrichment Operator",
          name: "AuthorityConformationEnrichmentOperator",
          url: "Operator/AuthorityConformationEnrichmentOperator",
          form: (
            <AuthorityConformationEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: PredicateConformationEnrichmentOperator,
          title: "Predicate Conformation Enrichment Operator",
          name: "PredicateConformationEnrichmentOperator",
          url: "Operator/PredicateConformationEnrichmentOperator",
          form: (
            <PredicateConformationEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: GeoDistanceEnrichmentOperator,
          title: "GeoDistance Enrichment Operator",
          name: "GeoDistanceEnrichmentOperator",
          url: "Operator/GeoDistanceEnrichmentOperator",
          form: (
            <GeoDistanceEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: SparqlUpdateEnrichmentOperator,
          title: "Sparql Update Enrichment Operator",
          name: "SparqlUpdateEnrichmentOperator",
          url: "Operator/SparqlUpdateEnrichmentOperator",
          form: (
            <SparqlUpdateEnrichmentOperator
              parentCallback={this.callbackFunction}
            />
          ),
        },
        {
          src: MergeEnrichmentOperator,
          title: "Merge Enrichment Operator",
          name: "MergeEnrichmentOperator",
          url: "Operator/MergeEnrichmentOperator",
          form: (
            <MergeEnrichmentOperator parentCallback={this.callbackFunction} />
          ),
        },
        {
          src: CloneEnrichmentOperator,
          title: "Clone Enrichment Operator",
          name: "CloneEnrichmentOperator",
          url: "Operator/CloneEnrichmentOperator",
          form: (
            <CloneEnrichmentOperator parentCallback={this.callbackFunction} />
          ),
        },
        {
          src: SparqlModelReader,
          title: "SparQL Model Reader",
          name: "SparqlModelReader",
          url: "Reader/SparqlModelReader",
          form: <SparqlModelReader parentCallback={this.callbackFunction} />,
        },
      ],
    };
  }

  getBaseUrl = () => {
    var re = new RegExp(/^.*\//);
    return re.exec(window.location.href);
  };

  callbackFunction = (properties) => {
    console.log(properties);
    this.setState({
      formProperties: properties,
    });
  };

  componentDidMount() {
    var graphCanvas = new LGraphCanvas("#mycanvas", this.state.graph);
    this.state.graph.start();

    //double click on a node will render a form on the UI
    graphCanvas.onShowNodePanel = (node) => {
      this.setState({
        node: node,
      });
    };
    graphCanvas.show_info = false;

    //returns the prefixes
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

    if (this.state.userInput === "" || this.state.namespace === "") {
      this.setState({
        isDisabled: true,
      });
    }

    const parser = new N3.Parser();
    fetch(URI + "/shapes")
      .then(function (response) {
        return response.text();
      })
      .then((content) => {
        parser.parse(content, (error, quad, prefixes) => {
          if (quad && quad.predicate.id.includes("targetClass")) {
            this.showNode(quad.object.id);
          } else {
          }
          //  console.log("No node returned.");
        });
      });
  }

  showNode = (quadOb) => {
    this.state.componentArray.map((comp, key) => {
      if (quadOb.includes(comp.name)) {
        comp.src.title = comp.title;
        LiteGraph.registerNodeType(comp.url, comp.src);
      }
    });
  };

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
    console.log(node);
    if (node.type === "Operator/LinkingEnrichmentOperator") {
      var inputLinkInGraph1 = this.state.graph.links[node.inputs[0].link];
      var inputLinkInGraph2 = this.state.graph.links[node.inputs[1].link];
      if (inputLinkInGraph1) {
        var inputOriginNode1 = this.state.graph.getNodeById(
          inputLinkInGraph1.origin_id
        ).properties.name;
      }
      if (inputLinkInGraph2) {
        var inputOriginNode2 = this.state.graph.getNodeById(
          inputLinkInGraph2.origin_id
        ).properties.name;
      }
      console.log(inputOriginNode1);

      return {
        first: inputOriginNode1,
        second: inputOriginNode2,
      };
    } else {
      for (let link in node.inputs) {
        if (node.inputs[link].link) {
          console.log(node.inputs[link].link);
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
    }
  };

  saveConfig = () => {
    var data = this.state.graph.serialize();
    //use N3
    const myQuad = DataFactory.quad(
      namedNode("https://ruben.verborgh.org/profile/#me"),
      namedNode("http://xmlns.com/foaf/0.1/givenName"),
      literal("Ruben", "en"),
      defaultGraph()
    );

    var writer = new N3.Writer(
      {
        prefixes: this.state.prefixes,
      },
      { format: "N-Triples" }
    );

    var parser = new N3.Parser({ format: "N3", blankNodePrefix: "" });
    console.log(this.state.formProperties);
    data.nodes.map((node, key) => {
      writer.addQuad(
        namedNode("urn:example:demo/" + node.properties.name),
        namedNode("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), //predicate
        namedNode("http://w3id.org/deer/" + node.type.split("/")[1]) //object
      );

      // if (this.getOutputLinks(node)) {
      //   //if it has output links, check and add a quad
      //   var readerTargetNode = this.getOutputLinks(node);
      //   writer.addQuad(
      //     namedNode("urn:example:demo/" + node.properties.name),
      //     namedNode("http://w3id.org/fcage/" + "hasOutput"),
      //     namedNode("urn:example:demo/" + readerTargetNode.properties.name)
      //   );
      // }
      //if it has an input link, check and add a quad
      if (node.inputs) {
        if (node.type === "Operator/LinkingEnrichmentOperator") {
          var inputs = this.getInputLink(node);
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/fcage/" + "hasInput"),
            writer.list([
              namedNode("urn:example:demo/" + inputs.first),
              namedNode("urn:example:demo/" + inputs.second),
            ])
          );
        } else {
          var originInputNode = this.getInputLink(node);
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/fcage/" + "hasInput"),
            namedNode("urn:example:demo/" + originInputNode.properties.name)
          );
        }
      }

      //File Model Reader
      if (node.type === "Reader/FileModelReader") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type === "Reader/FileModelReader"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.fromUri = this.state.formProperties.fromUri;
          node.properties.fromPath = this.state.formProperties.fromPath;
        }
        if (node.properties.fromUri) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "fromUri"),
            literal(node.properties.fromUri)
          );
        }
        if (node.properties.fromPath) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "fromPath"),
            literal(node.properties.fromPath)
          );
        }
      }

      //Sparql Model Reader
      if (node.type === "Reader/SparqlModelReader") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type === "Reader/SparqlModelReader"
        ) {
          node.properties.name = this.state.formProperties.name;
          node.properties.fromEndpoint = this.state.formProperties.fromEndpoint;
          node.properties.sparqlDescribeOf = this.state.formProperties.useSparqlDescribeOf;
          node.properties.useSparqlConstruct = this.state.formProperties.useSparqlConstruct;
        }
        if (node.properties.fromEndpoint) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "fromEndpoint"),
            namedNode(node.properties.fromEndpoint)
          );
        }
        if (node.properties.sparqlDescribeOf) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "useSparqlDescribeOf"),
            namedNode(node.properties.sparqlDescribeOf)
          );
        }

        if (node.properties.useSparqlConstruct) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "useSparqlConstruct"),
            literal(node.properties.useSparqlConstruct)
          );
        }
      }

      //File Model Writer
      if (node.type === "Writer/FileModelWriter") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type === "Writer/FileModelWriter"
        ) {
          node.properties.name = this.state.formProperties.name;
          node.properties.outputFile = this.state.formProperties.outputFile;
          node.properties.outputFormat = this.state.formProperties.outputFormat;
        }
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
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/FilterEnrichmentOperator"
        ) {
          node.properties.name = this.state.formProperties.name;
          node.properties.selector = this.state.formProperties.selector;
          node.properties.resource = this.state.formProperties.resource;
          node.properties.sparqlConstructQuery = this.state.formProperties.sparqlConstructQuery;
        }
        if (node.properties.selector) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "selector"), //predicate
            writer.blank([
              {
                predicate: namedNode(
                  "http://w3id.org/deer/" + node.properties.selector
                ),
                object: namedNode(node.properties.resource),
              },
            ])
          );
        } else if (node.properties.sparqlConstructQuery) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "sparqlConstructQuery"),
            literal(node.properties.sparqlConstructQuery)
          );
        }
      }
      //Dereferencing enrichment Operator
      if (node.type === "Operator/DereferencingEnrichmentOperator") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/DereferencingEnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.lookUpPrefix = this.state.formProperties.lookUpPrefix;
          node.properties.dereferencingProperty = this.state.formProperties.dereferencingProperty;
          node.properties.importProperty = this.state.formProperties.importProperty;
        }
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
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/GeoFusionEnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.fusionAction = this.state.formProperties.fusionAction;
          node.properties.mergeOtherStatements = this.state.formProperties.mergeOtherStatements;
        }
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
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/AuthorityConformationEnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.sourceSubjectAuthority = this.state.formProperties.sourceSubjectAuthority;
          node.properties.targetSubjectAuthority = this.state.formProperties.targetSubjectAuthority;
        }
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
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/PredicateConformationEnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.sourcePredicate = this.state.formProperties.sourcePredicate;
          node.properties.targetPredicate = this.state.formProperties.targetPredicate;
        }
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
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/GeoDistanceEnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.selectPredicate = this.state.formProperties.selectPredicate;
          node.properties.distancePredicate = this.state.formProperties.distancePredicate;
        }
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "selectPredicate"),
          namedNode(node.properties.selectPredicate)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "distancePredicate"),
          namedNode(node.properties.distancePredicate)
        );
      }

      if (node.type === "Operator/LinkingEnrichmentOperator") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/LinkingEnrichmentOperator"
        ) {
          node.properties.name = this.state.formProperties.name;
          node.properties.specFile = this.state.formProperties.specFile;
          node.properties.linksPart = this.state.formProperties.linksPart;
          node.properties.selectMode = this.state.formProperties.selectMode;
          node.properties.linkSpecification = this.state.formProperties.linkSpecification;
          node.properties.linkingPredicate = this.state.formProperties.linkingPredicate;
          node.properties.threshold = this.state.formProperties.threshold;
        }
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "specFile"),
          literal(node.properties.specFile)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "linksPart"),
          literal(node.properties.linksPart)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "selectMode"),
          literal(node.properties.selectMode)
        );
        if (node.properties.linkSpecification) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "linkSpecification"),
            literal(node.properties.linkSpecification)
          );
        }
        if (node.properties.linkingPredicate) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "linkingPredicate"),
            literal(node.properties.linkingPredicate)
          );
        }
        if (node.properties.threshold) {
          writer.addQuad(
            namedNode("urn:example:demo/" + node.properties.name),
            namedNode("http://w3id.org/deer/" + "threshold"),
            literal(node.properties.threshold)
          );
        }
      }

      //NER Enrichment Operator
      if (node.type === "Operator/NEREnrichmentOperator") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/NEREnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.literalProperty = this.state.formProperties.literalProperty;
          node.properties.importProperty = this.state.formProperties.importProperty;
          node.properties.neType = this.state.formProperties.neType;
          node.properties.foxUrl = this.state.formProperties.foxUrl;
        }

        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "literalProperty"),
          namedNode(node.properties.literalProperty)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "importProperty"),
          namedNode(node.properties.distancePredimportPropertyicate)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "foxUrl"),
          namedNode(node.properties.foxUrl)
        );
        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "neType"),
          literal(node.properties.neType)
        );
      }

      //Sparql Update Enrichment Operator
      if (node.type === "Operator/SparqlUpdateEnrichmentOperator") {
        if (
          this.state.formProperties &&
          this.state.formProperties.node.type ===
            "Operator/NEREnrichmentOperator"
        ) {
          node.properties.name = this.state.properties.name;
          node.properties.sparqlUpdateQuery = this.state.formProperties.sparqlUpdateQuery;
        }

        writer.addQuad(
          namedNode("urn:example:demo/" + node.properties.name),
          namedNode("http://w3id.org/deer/" + "sparqlUpdateQuery"),
          literal(node.properties.sparqlUpdateQuery)
        );
      }
    });
    writer.end((error, result) => {
      console.log(result);
      this.submitConfig(result);
      result = "";
    });
  };

  //Download the results
  downloadResults = (index) => {
    fetch(
      URI +
        "/result/" +
        this.state.requestID +
        "/" +
        this.state.availableFiles[index]
    ).then((response) => {
      response.blob().then((blob) => {
        let url = window.URL.createObjectURL(blob);
        let a = document.createElement("a");
        a.href = url;
        a.download = this.state.availableFiles[index];
        a.click();
      });
    });
  };

  submitConfig = (result) => {
    var data = new Blob([result], { type: "text/ttl" });
    var file = new File([data], "config.ttl");

    var formData = new FormData();
    formData.append("config", file);
    fetch(URI + "/submit", {
      method: "POST",
      body: formData,
    })
      .then((response) => response.json())
      .then((res) => {
        if (res.error && res.error.code === -1) {
          this.setState({
            visible: true,
          });
        } else if (res.requestId) {
          this.setState({
            requestID: res.requestId,
            showLogButton: true,
          });
          this.interval = setInterval(this.getStatusForRequest, 1000);
        }
      });
  };

  getStatusForRequest = () => {
    fetch(URI + "/status/" + this.state.requestID)
      .then(function (response) {
        return response.json();
      })
      .then((content) => {
        if (content.status.code === 2) {
          clearInterval(this.interval);
          this.setState({
            requestCompleteModal: true,
            showConfigButton: true,
          });
          this.getResults();
        }
      });
  };

  getResults = () => {
    fetch(URI + "/results/" + this.state.requestID)
      .then(function (response) {
        return response.json();
      })
      .then((content) => {
        console.log(content.availableFiles);
        this.setState({
          availableFiles: content.availableFiles,
        });
      });
  };

  showLogs = () => {
    fetch(URI + "/logs/" + this.state.requestID).then(function (response) {
      let a = document.getElementById("downloadlink");
      a.href = response.url;
      a.click();
    });
  };
  toggle = () => {
    this.setState({
      visible: !this.state.visible,
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
      isDisabled: false,
    });
  };

  handleNamespaceChange = (event) => {
    this.setState({
      namespace: event.target.value,
      isDisabled: false,
    });
  };

  addNewPrefixes = (e) => {
    this.setState({
      addNewPrefixes: this.state.addNewPrefixes.concat(this.state.userInput),
    });
    this.state.prefixes[this.state.userInput] = this.state.namespace;
  };

  saveResults = () => {
    this.setState({
      showResultModal: !this.state.showResultModal,
    });
  };

  toggleResultModal = () => {
    this.setState({
      showResultModal: !this.state.showResultModal,
    });
  };

  toggleRequestCompleteModal = () => {
    this.setState({
      requestCompleteModal: !this.state.requestCompleteModal,
    });
  };

  uploadFiles = () => {};

  render() {
    const options = _.map(this.state.prefixOptions, (opt, index) => ({
      key: opt,
      text: opt,
      value: opt,
    }));

    return (
      <div className="content">
        <Modal isOpen={this.state.visible} toggle={this.toggle}>
          <ModalHeader toggle={this.toggle}>
            Incorrect Configuration
          </ModalHeader>
          <ModalBody>
            Please input all the required fields and connections.
          </ModalBody>
        </Modal>

        <Modal
          isOpen={this.state.requestCompleteModal}
          toggle={this.toggleRequestCompleteModal}
        >
          <ModalHeader toggle={this.toggleRequestCompleteModal}>
            Status
          </ModalHeader>
          <ModalBody>
            The results are ready. You can download them by clicking 'Show
            Results'.
          </ModalBody>
        </Modal>

        <Modal
          isOpen={this.state.showResultModal}
          toggle={this.toggleResultModal}
        >
          <ModalHeader toggle={this.toggleResultModal}>Results</ModalHeader>
          <ModalBody>
            <Table>
              <thead>
                <tr>
                  <th>File</th>
                  <th>Download</th>
                </tr>
              </thead>
              <tbody>
                {this.state.availableFiles.map((file, index) => (
                  <tr key={index}>
                    <td>{file}</td>
                    <td>
                      <Button onClick={() => this.downloadResults(index)}>
                        <i
                          className="fa fa-download"
                          style={{ color: `white` }}
                        />{" "}
                        Download
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </ModalBody>
        </Modal>
        <Row>
          <Col lg="9" md="9" sm="9">
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
                            disabled={this.state.isDisabled}
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
          <Col lg="3" md="3" sm="3">
            <Card className="card-stats">
              <div className="numbers">
                <CardTitle tag="p">Upload Files</CardTitle>
                <p />
              </div>
              <CardBody>
                {/* <div class="form-group">
                  <input
                    type="file"
                    class="form-control-file"
                    id="exampleFormControlFile1"
                  ></input>
                </div> */}
                <Row>
                  <Col md="9">
                    {" "}
                    <input
                      id="input-b2"
                      name="input-b2"
                      type="file"
                      class="file"
                      data-show-preview="false"
                    ></input>
                  </Col>
                  <Col md="3">
                    {" "}
                    <Button
                      className="btn-round uploadBtn"
                      color="primary"
                      onClick={this.uploadFiles}
                    >
                      Upload
                    </Button>
                  </Col>
                </Row>
              </CardBody>
              <hr />
              <CardFooter></CardFooter>
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
                <canvas id="mycanvas" height="600" width="1000"></canvas>{" "}
              </CardBody>
              <CardFooter>
                <hr />
                <div className="stats">
                  {/* <a
                    download="config.ttl"
                    id="downloadlink"
                    //style={{ display: "none" }}
                    ref="file"
                  > */}
                  <Button onClick={this.saveConfig}>
                    <i className="fa fa-cog" style={{ color: `white` }} /> Run
                    Configuration
                  </Button>
                  {/* </a> */}
                  {this.state.showConfigButton ? (
                    <Button
                      onClick={this.saveResults}
                      style={{ marginLeft: `10px` }}
                    >
                      <i
                        className="fa fa-sticky-note"
                        style={{ color: `white` }}
                      />{" "}
                      Show results
                    </Button>
                  ) : (
                    ""
                  )}
                  {this.state.showLogButton ? (
                    <a
                      target="_blank"
                      id="downloadlink"
                      //style={{ display: "none" }}
                      ref="file"
                    >
                      <Button
                        onClick={this.showLogs}
                        style={{ marginLeft: `10px` }}
                      >
                        <i className="fa fa-cog" style={{ color: `white` }} />{" "}
                        Logs
                      </Button>
                    </a>
                  ) : (
                    ""
                  )}
                </div>
              </CardFooter>
            </Card>
          </Col>
          {/* <Modal isOpen={this.state.isModalOpen} toggle={this.toggle}>
            <ModalHeader toggle={this.toggle}>Display Config</ModalHeader>
            <ModalBody></ModalBody>
          </Modal> */}
          <Col md="3">
            {this.state.componentArray.map((comp, key) => {
              if (this.state.node.title === comp.title) {
                return comp.form;
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

export default Dashboard;