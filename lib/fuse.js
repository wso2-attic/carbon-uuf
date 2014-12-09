//public function declarations
var getFile, toRelativePath, cleanupAncestors,
    getUnitPath, getMatchedUnitDefinitions, getZoneDefinition, getUnitDefinition,
    getUnitDefinitions, getLayoutPath;

(function () {
    //private
    var log = new Log('fuse.core');
    var lookUpTable = null;
    var definitions = null;

    var initLookUp = function (definitions) {
        if (lookUpTable === null) {
            lookUpTable = {};
            for (var i = 0; i < definitions.length; i++) {
                var definition = definitions[i];
                lookUpTable[definition.name] = i;
            }
        }
    };

    var isMatched = function (definition) {
        var urlMatch = function (pattern) {
            var uriMatcher = new URIMatcher(request.getRequestURI());
            return Boolean(uriMatcher.match('/{appName}' + pattern));
        };
        var config = {'theme': 'default'};
        var predicateStr = definition.definition.predicate;
        if (predicateStr) {
            var js = 'function(config,urlMatch){ return ' + predicateStr + ';}';
            return Boolean(eval(js)(config, urlMatch));
        }
        return false;
        //return (definition.name == 'theme' || definition.name == 'login-page' || definition.name == 'am-publisher-logo');
    };

    var getAncestorModels = function (unit) {
        var unitModel = getUnitDefinition(unit);
        var ancestors = [unitModel];
        var parentName;
        while ((parentName = unitModel.definition.extends) != null) {
            unitModel = getUnitDefinition(parentName);
            ancestors.push(unitModel);
        }
        return ancestors;
    };



    //public
    getMatchedUnitDefinitions = function () {
        var unitDefinitions = getUnitDefinitions();
        var matched = [];
        for (var i = 0; i < unitDefinitions.length; i++) {
            var unitDefinition = unitDefinitions[i];
            if (isMatched(unitDefinition)) {
                matched.push(unitDefinition);
            }
        }
        return matched;
    };

    getUnitDefinition = function (unit) {
        var definitions = getUnitDefinitions();
        initLookUp(definitions);
        var model = definitions[lookUpTable[unit]];
        if (!model) {
            throw 'erro';
            //TODO:fix
            log.warn('non exesing unit ' + unit)
        }
        return model;
    };

    getUnitDefinitions = function () {
        if (definitions !== null) {
            return definitions;
        } else {
            definitions = [];
        }

        var unitDirs = new File('/units').listFiles();
        for (var i = 0; i < unitDirs.length; i++) {
            var unitDir = unitDirs[i];
            if (unitDir.isDirectory()) {

                var unitName = unitDir.getName();
                var unitModel = {name: unitName};

                // unit definition is read form is the <unit name>.json file.
                // if doesn't exits it will be an empty json.
                var definitionFile = new File(fuse.getUnitPath(unitName) + '/' + unitName + '.json');
                if (definitionFile.isExists() && !definitionFile.isDirectory()) {
                    unitModel.definition = require(definitionFile.getPath());
                } else {
                    log.warn('[' + requestId + '] for unit "' + unitName + '", unable to find a definition file');
                    unitModel.definition = {};
                }

                // add the information derived by parsing hbs file to the same model
                var hbsMetadata = getHbsMetadata(unitName);
                unitModel.zones = hbsMetadata.zones;
                if (hbsMetadata.layout) {
                    unitModel.layout = hbsMetadata.layout;
                }

                definitions.push(unitModel);
            }
        }

        initLookUp(definitions);

        return definitions;
    };


    getLayoutPath = function (layout) {
        return '/layouts/' + layout + '.hbs';
    };

    getHbsMetadata = function (unit) {
        var zoneDef = {'zones': []};
        var hbsFile = new File(fuse.getUnitPath(unit) + '/' + unit + '.hbs');
        if (!hbsFile.isExists()) {
            return zoneDef;
        }
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


    getUnitPath = function (unit) {
        return '/units/' + unit;
    };

    cleanupAncestors = function (units) {
        log.info(unit);
        var toDelete = {};
        var len = units.length;
        for (var i = 0; i < len; i++) {
            var unit = units[i];
            if (!toDelete[unit]) {
                var ancestors = getAncestorModels(unit.name);
                for (var j = 1; j < ancestors.length; j++) {
                    toDelete[ancestors[j].name] = unit;
                }
            }
        }
        while (len--) {
            if (toDelete[units[len]]) {
                log.debug(
                    '[' + requestId + '] unit "' + units[len] +
                    '" is overridden by "' + toDelete[units[len]] + '"'
                );
                units.splice(len, 1);
            }
        }
    };

    toRelativePath = function (path) {
        var start = 0;
        if (path.lastIndexOf('/units/', 0) == 0) {
            start = 7; // len('/units/')
        }
        var slashPos = path.indexOf('/', 7);
        return {
            unit: path.substring(start, slashPos),
            path: path.substr(slashPos)
        }
    };

    /**
     * Get a file inside a unit by relative path. if the file is not available in the given unit,
     * the closest ancestor's file will be returned. if an optional suffix is used the relative path is
     * calculated as ( path + < unit name > + opt_suffix ). if no such a file exists a returned file object will
     * point to provided unit's non-existing file location (not to any ancestors).
     *
     * @param unitName name of the unit
     * @param path path relative to unit root.
     * @param opt_suffix
     * @returns {File}
     */
    getFile = function (unitName, path, opt_suffix) {
        var slashPath = ((path[0] === '/') ? '' : '/') + path;
        var selfFileName = '';
        var fileName = '';
        if (opt_suffix) {
            selfFileName = unitName + opt_suffix;
            slashPath = slashPath + ((slashPath[slashPath.length - 1] === '/') ? '' : '/');
        }

        var selfFile = new File(getUnitPath(unitName) + slashPath + selfFileName);
        if (selfFile.isExists()) {
            log.debug(
                '[' + requestId + '] for unit "' + unitName + '" file resolved : "'
                + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
            );

            return selfFile;
        }

        var ancestors = getAncestorModels(unitName);
        for (var i = 1; i < ancestors.length; i++) {
            var ancestorName = ancestors[i].name;
            if (opt_suffix) {
                fileName = ancestorName + opt_suffix;
            }
            var file = new File(getUnitPath(ancestorName) + slashPath + fileName);
            if (file.isExists()) {
                log.debug(
                    '[' + requestId + '] for unit "' + unitName + '" file resolved : "'
                    + slashPath + selfFileName + '" -> "' + file.getPath() + '"'
                );
                return file;
            }
        }
        log.debug(
            '[' + requestId + '] for unit "' + unitName + '" (non-excising) file resolved : "'
            + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
        );
        return selfFile;
    };

})();
