var erd = joint.shapes.erd;

var graph = new joint.dia.Graph();

var paper = new joint.dia.Paper({
    el: document.getElementById('paper'),
    width: 695,
    height: 600,
    gridSize: 1,
    model: graph,
    linkPinning: false,
    interactive: false,
    linkConnectionPoint: joint.util.shapePerimeterConnectionPoint
});

var app = new erd.Entity({

    position: { x: 100, y: 200 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'App',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.outer, .inner': {
            fill: '#31d0c6',
            stroke: 'none',
            filter: { name: 'dropShadow',  args: { dx: 0.5, dy: 2, blur: 2, color: '#333333' }}
        }
    }
});

var mvn = new erd.IdentifyingRelationship({

    position: { x: 350, y: 190 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: '     Maven \n Dependancy',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.inner': {
            fill: '#7c68fd',
            stroke: 'none'
        },
        '.outer': {
            fill: 'none',
            stroke: '#7c68fd',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 1, color: '#333333' }}
        }
    }
});

var component = new erd.Entity({

    position: { x: 530, y: 200 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Component',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.outer, .inner': {
            fill: '#31d0c6',
            stroke: 'none',
            filter: { name: 'dropShadow',  args: { dx: 0.5, dy: 2, blur: 2, color: '#333333' }}
        }
    }
});


/*
var isa = new erd.ISA({

    position: { x: 125, y: 300 },
    attrs: {
        text: {
            text: 'ISA',
            fill: '#ffffff',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        polygon: {
            fill: '#fdb664',
            stroke: 'none',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 1, color: '#333333' }}
        }
    }
});

var number = new erd.Key({

    position: { x: 1, y: 90 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Number',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.outer, .inner': {
            fill: '#feb662',
            stroke: 'none'
        },
        '.outer': {
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 2, color: '#222138' }}
        }
    }
});

var employeeName = new erd.Normal({

    position: { x: 75, y: 30 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Name',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.outer': {
            fill: '#fe8550',
            stroke: '#fe854f',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 2, color: '#222138' }}
        }
    }
});

var skills = new erd.Multivalued({

    position: { x: 150, y: 90 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Skills',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0px 1px #333333' }
        },
        '.inner': {
            fill: '#fe8550',
            stroke: 'none',
            rx: 43,
            ry: 21

        },
        '.outer': {
            fill: '#464a65',
            stroke: '#fe8550',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 2, color: '#222138' }}
        }
    }
});

var amount = new erd.Derived({

    position: { x: 440, y: 80 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Amount',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.inner': {
            fill: '#fca079',
            stroke: 'none',
            'display': 'block'
        },
        '.outer': {
            fill: '#464a65',
            stroke: '#fe854f',
            'stroke-dasharray': '3,1',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 2, color: '#222138' }}
        }
    }
});

var uses = new erd.Relationship({

    position: { x: 300, y: 390 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: 'Uses',
            'letter-spacing': 0,
            style: { 'text-shadow': '1px 0 1px #333333' }
        },
        '.outer': {
            fill: '#797d9a',
            stroke: 'none',
            filter: { name: 'dropShadow',  args: { dx: 0, dy: 2, blur: 1, color: '#333333' }}
        }
    }
});

// Create new shapes by cloning

var salesman = employee.clone().translate(0, 200).attr('text/text', 'Salesman');

var date = employeeName.clone().position(590, 80).attr('text/text', 'Date');

var car = employee.clone().position(430, 400).attr('text/text', 'Company car');

var plate = number.clone().position(405, 500).attr('text/text', 'Plate');
*/

// Helpers

var createLink = function(elm1, elm2) {

    var myLink = new erd.Line({
        source: { id: elm1.id },
        target: { id: elm2.id }
    });

    return myLink.addTo(graph);
};

var createLabel = function(txt) {
    return {
        labels: [{
            position: -20,
            attrs: {
                text: { dy: -8, text: txt, fill: '#000' },
                rect: { fill: 'none' }
            }
        }]
    };
};

// Add shapes to the graph

graph.addCells([app, mvn, component]);

createLink(app, mvn).set(createLabel('1'));
createLink(employee, mvn);

paper.on('cell:pointerdown', 
    function(cellView, evt, x, y) { 
        console.log('cell view ' + cellView.model.id + ' was clicked'); 
    }
);
