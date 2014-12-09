var log = new Log('fuse.handlebars');

var Handlebars = require('handlebars-v2.0.0.js').Handlebars;

var getScope = function (unit) {
    log.info(appName);
    return {
        self: {
            publicURL: '/' + appName + '/public/' + unit
        }
    };
};

Handlebars.registerHelper('defineZone', function (zoneName, zoneContent) {
    var result = '';
    var zone = Handlebars.Utils.escapeExpression(zoneName);
    fuseState.currentZone = zone;
    var unitsToRender = fuseState.zones[zone] || [];

    if (zoneContent['fn'] && unitsToRender.length == 0) {
        return zoneContent.fn(this).trim();
    }

    for (var i = 0; i < unitsToRender.length; i++) {
        var unit = unitsToRender[i];
        var template = fuse.getFile(unit, '', '.hbs');
        log.debug('[' + requestId + '] for zone "' + zone + '" including template :"' + template.getPath() + '"');
        result += Handlebars.compileFile(template)(getScope(unit));
    }
    fuseState.currentZone = '';
    return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('zone', function (context, zoneContent) {
    if (fuseState.currentZone === null) {
        return 'zone_' + context;
    }
    if (context == fuseState.currentZone) {
        return zoneContent.fn(this).trim();
    } else {
        return '';
    }
});

Handlebars.registerHelper('layout', function (layoutName) {
    if (fuseState.currentZone === null) {
        return 'layout_' + layoutName;
    } else {
        return '';
    }
});

Handlebars.compileFile = function (file) {
    //TODO: remove this overloaded argument
    var f = (typeof file === 'string') ? new File(file) : file;

    if (!Handlebars.cache) {
        Handlebars.cache = {};
    }

    if (Handlebars.cache[f.getPath()] != null) {
        return Handlebars.cache[f.getPath()];
    }

    f.open('r');
    log.debug('[' + requestId + '] reading hbs file ' + f.getPath() + '"');
    var content = f.readAll().trim();
    f.close();
    var compiled = Handlebars.compile(content);
    Handlebars.cache[f.getPath()] = compiled;
    return compiled;
};
