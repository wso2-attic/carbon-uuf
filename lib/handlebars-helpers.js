var log = new Log('fuse.handlebars');

var Handlebars = require('handlebars-v2.0.0.js').Handlebars;

var getScope = function (unit) {
    return {
        self: {
            publicURL: '/fuse/public/' + unit
        }
    };
};

Handlebars.registerHelper('defineZone', function (zoneName) {
    var result = '';
    var zone = Handlebars.Utils.escapeExpression(zoneName);
    currentZones = zone;
    var unitsToRender = zones[zone] || [];
    for (var i = 0; i < unitsToRender.length; i++) {
        var unit = unitsToRender[i];
        var template = fuse.getFile(unit, '', '.hbs');
        log.debug('[' + requestId + '] for zone "' + zone + '" including template :"' + template.getPath() + '"');
        result += Handlebars.compileFile(template)(getScope(unit));
    }
    return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('zone', function (context, zoneContent) {
    if (context == currentZones) {
        return zoneContent.fn(this).trim();
    } else {
        return '';
    }
});

Handlebars.registerHelper('layout', function (zoneName) {

});

Handlebars.compileFile = function (file) {
    var f = (typeof file === 'string') ? new File(file) : file;

    if (!Handlebars.cache) {
        Handlebars.cache = {};
    }

    if (Handlebars.cache[f.getPath()] != null) {
        return Handlebars.cache[f.getPath()];
    }

    f.open('r');
    var content = f.readAll().trim();
    f.close();
    var compiled = Handlebars.compile(content);
    Handlebars.cache[f.getPath()] = compiled;
    return compiled;
};
