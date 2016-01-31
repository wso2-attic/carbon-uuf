var erd = joint.shapes.erd;

var graph = new joint.dia.Graph();

var paper = new joint.dia.Paper({
    el: document.getElementById('paper'),
    width: 695,
    height: 340,
    gridSize: 1,
    model: graph,
    linkPinning: false,
    interactive: false,
    linkConnectionPoint: joint.util.shapePerimeterConnectionPoint
});

var element = function(name, x, y){
    return new erd.Entity({
        position: { x: x, y: y },
        attrs: {
            text: {
                fill: '#ffffff',
                text: name
            }
        }
    });
};


var mvn = new erd.IdentifyingRelationship({

    position: { x: 200, y: 0 },
    attrs: {
        text: {
            fill: '#ffffff',
            text: '     Maven \n Dependancy',
            'letter-spacing': 0,
        }
    }
});


var createLink = function(elm1, elm2) {

    var myLink = new erd.Line({
        source: { id: elm1.id },
        target: { id: elm2.id }
    });

    return myLink.addTo(graph);
};

var createLabel = function(txt,pos) {
    return {
        labels: [{
            position: pos||25,
            attrs: {
                text: { offset: {dy:1000}, text: txt, fill: '#000' },
                rect: { fill: '#fff' }
            },
            props:{ }
        }]
    };
};

// Add shapes to the graph

var app = element('App', 0, 10) ;
var component = element('Component', 325, 10) ;
var layout = element('Layout', 1505, 120) ;
var page = element('Page', 165, 95) ;
var zone = element('Zone', 165, 175) ;
var unit = element('Unit', 165, 255) ;

graph.addCells([app, mvn, component, unit, zone, page, layout ]);

createLink(app, mvn).set(createLabel('1'));
createLink(mvn, component).set(createLabel('n'));
createLink(app, unit).set(createLabel('n')).set('vertices', [{ x: 100, y: 200 }]).set('smooth', true);
createLink(component, unit).set(createLabel('n')).set('vertices', [{ x: 370, y: 200 }]).set('smooth', true);
createLink(zone, unit).set(createLabel('n',12));
createLink(app, page).set(createLabel('n'));
createLink(component, page).set(createLabel('n'));
createLink(zone, page).set(createLabel('n',8));

paper.on('cell:pointerdown', 
    function(cellView, evt, x, y) { 
        var attrs = cellView.model.attributes.attrs;
        var titleEl;
        if(attrs){
            var text = attrs.text.text; 
            titleEl = $('h3').filter(function () { return $(this).html() == text; });
        }else{
            var source = graph.getCell(cellView.model.attributes.source.id).attributes.attrs.text.text;
            var target = graph.getCell(cellView.model.attributes.target.id).attributes.attrs.text.text;
            titleEl = $('h3').filter(function () { 
                var html = $(this).text();
                return html.indexOf(source)>-1 &&  html.indexOf(target)>-1;
            });
        }
        hideAll();
        doSection(titleEl,function(el){el.show()});
    }
);

var doSection = function(el,func,notFirst){
    var $el  = $(el);
    var next = $el.next();
    if(!notFirst || el.is('p') || el.is('figure')){
        func($el);
        return doSection(next, func, true);
    }else{
        return;
    }
};

var hideAll = function(){
    $('h3').each(function(i,el){
        var els = doSection(el, function(el){el.hide()});
    });
};

$(hideAll);
