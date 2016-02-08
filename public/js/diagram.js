var erd = joint.shapes.erd;

var graph = new joint.dia.Graph();

var paper = new joint.dia.Paper({
    el: document.getElementById('paper'),
    width: 795,
    height: 340,
    gridSize: 1,
    model: graph,
    linkPinning: false,
    interactive: false,
    linkConnectionPoint: joint.util.shapePerimeterConnectionPoint
});

var highlighted;

var element = function(name, x, y){
    return new erd.Entity({
        position: { x: x, y: y },
        attrs: {
            text: {
                fill: '#ffffff',
                text: name
            },
            '.outer': {
                 fill: 'rgb(232, 155, 92)',
                 stroke: 'rgb(158, 78, 14)',
            }
        },
    });
};


var createLink = function(elm1, elm2) {

    var myLink = new erd.Line({
        source: { id: elm1.id },
        smooth : true,
        attrs : {
            '.connection' : {stroke: '#aaa', 'stroke-dasharray': '4 2'}
        },
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
            }
        }]
    };
};

// Add shapes to the graph

var app = element('App', 200, 10) ;
app.resize(200,80);
var component = element('Component', 550, 120) ;
component.resize(200,80);
component.attr('.outer/fill','#7f8c8d');
component.attr('.outer/stroke','black');
var page = element('Page', 10, 140) ;
var fragment = element('Fragment', 190, 140) ;
var layout = element('Layout', 370, 140) ;

graph.addCells([app, component, fragment, page, layout ]);


var refLinkStroke = {'stroke-dasharray': 0};
createLink(app, fragment).set(createLabel('n')).attr('.connection', refLinkStroke);
createLink(app, page).set(createLabel('n')).attr('.connection', refLinkStroke);
createLink(app, layout).set(createLabel('n')).attr('.connection', refLinkStroke);

createLink(app, component).set(createLabel('n'));
createLink(page, fragment).set(createLabel('n',16));
createLink(fragment, fragment).set(createLabel('n'))
         .set('vertices', [{ x: 225, y: 250 },{x: 264,y: 278},{ x: 300, y: 250 }]) ;

createLink(page, layout)
         .set('labels', [{
                position: 25,
                attrs: {
                    text: { offset: {dy:1000}, text: 'n', fill: '#000' },
                    rect: { fill: '#fff' }
                }
            },{
                position: 385,
                attrs: {
                    text: { offset: {dy:1000}, text: '1', fill: '#000' },
                    rect: { fill: '#fff' }
                }
            }])
         .set('vertices', [{ x: 175, y: 300 },{ x: 325, y: 300 }]) ;
//.set('vertices', [{ x: 100, y: 200 }]

paper.on('cell:pointerdown', 
    function(cellView, evt, x, y) { 
        if(highlighted)
            highlighted.unhighlight();
        cellView.highlight();
        highlighted = cellView;
        var attrs = cellView.model.attributes.attrs;
        var titleEl;
        if(attrs && attrs.text){
            var text = attrs.text.text; 
            titleEl = $('h3').filter(function () { return $(this).html() == text; });
        }else{
            var source = graph.getCell(cellView.model.attributes.source.id).attributes.attrs.text.text;
            var target = graph.getCell(cellView.model.attributes.target.id).attributes.attrs.text.text;
            titleEl = $('h3').filter(function () { 
                var html = $(this).text();
                var sourcePos = html.indexOf(source);
                var targetPos = html.lastIndexOf(target);
                return sourcePos >-1 && targetPos >-1 && sourcePos != targetPos;
            });
        }
        hideAll();
        doSection(titleEl,function(el){el.show()});
    }
);

paper.on('blank:pointerclick', function(cellView) {
    if (highlighted){
        highlighted.unhighlight();
    }
    highlighted = null;
    showAll();
});

var doSection = function(el,func,notFirst){
    var $el  = $(el);
    var next = $el.next();
    if(!notFirst || el.is('p') || el.is('figure') || el.hasClass('highlight')){
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

var showAll = function(){
    $('h3').each(function(i,el){
        var els = doSection(el, function(el){el.show()});
    });
};


