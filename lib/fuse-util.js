//public function declarations
var getZoneDefinition, getUnitDefinitions, isMatched, getLayoutPath;

(function () {
    //private
    var log = new Log('fuse.util');
    var definitions = null;

    //public
    getUnitDefinitions = function () {
        if (definitions !== null) {
            return definitions;
        } else {
            definitions = [];
        }

        var unitDirs = new File('/units').listFiles();
        for (var i = 0; i < unitDirs.length; i++) {
            var dir = unitDirs[i];
            if (dir.isDirectory()) {
                var unitName = dir.getName();
                var definition = new File(fuse.getUnitPath(unitName) + '/' + unitName + '.json');
                if (definition.isExists() && !definition.isDirectory()) {
                    definitions.push({
                        'name': unitName,
                        'definition': require(definition.getPath())
                    });
                } else {
                    log.warn('[' + requestId + '] for unit "' + unitName + '", unable to find a definition file');
                    definitions.push({
                        'name': unitName,
                        'definition': {}
                    });
                }
            }
        }

        return definitions;
    };

    isMatched = function (definition) {
        return (definition.name == 'theme' || definition.name == 'login-page' || definition.name == 'am-publisher-logo');
    };

    getLayoutPath = function (layout) {
        return '/layouts/' + layout + '.hbs';
    };

    getZoneDefinition = function (unit) {
        var zoneDef = {'zones': []};
        var hbsFile = fuse.getFile(unit, '', '.hbs');
        if(!hbsFile.isExists()){
            return zoneDef;
        }
        currentZone = null;
        var output = handlebars.Handlebars.compileFile(hbsFile)({});
        var zonesAndLayouts = output.trim().split(/\s+/gm);
        for (var i = 0; i < zonesAndLayouts.length; i++) {
            var name = zonesAndLayouts[i];
            if (name.lastIndexOf('zone_', 0) === 0) {
                zoneDef.zones.push(name.substr(5));
            } else if (name.lastIndexOf('layout_', 0) === 0) {
                zoneDef.layout = name.substr(7);

            }
        }
        return zoneDef;
    };

})();
