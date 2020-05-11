var N3 = require('n3');
const { DataFactory } = N3;
const { namedNode, literal, defaultGraph, quad } = DataFactory;
const store = N3.Store();
store.addQuad(
  namedNode('http://ex.org/Pluto'), 
  namedNode('http://ex.org/type'),
  namedNode('http://ex.org/Dog')
);
store.addQuad(
  namedNode('http://ex.org/Mickey'),
  namedNode('http://ex.org/type'),
  namedNode('http://ex.org/Mouse')
);

const mickey = store.getQuads(namedNode('http://ex.org/Mickey'), null, null)[0];
console.log(mickey);
