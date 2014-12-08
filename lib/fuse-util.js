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
        return (definition.name == 'theme' || definition.name == 'login-page' || definition.name == 'logo' || definition.name == 'am-publisher-logo');
    };

    getLayoutPath = function (layout) {
        return '/layouts/' + layout + '.hbs';
    };

    getZoneDefinition = function (unit) {
        if (unit == 'theme') {
            return {'zones': ['topCss']};
        } else if (unit == 'login-page') {
            return {'layout': 'full-page', 'zones': ['main', 'topCss']};
        } else if (unit == 'am-publisher-logo') {
            return {'zones': ['brand', 'topCss']};
        } else if (unit == 'logo') {
            return {'zones': ['brand', 'topCss']};
        }
        return {};
    };

})();
