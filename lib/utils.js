function getConfigurations(isCachingEnabled) {
    // TODO: implement a proper caching mechanism
    var configs = require("configs.json");
    configs.isCachingEnabled = isCachingEnabled;
    return configs;
}

function getLookUpTable(configs) {
    // TODO: implement a proper caching mechanism
    var log = new Log("utils-app-context");

    function getLayoutsData(layoutsDir) {
        var layoutsData = {};
        var layoutsFiles = new File(layoutsDir).listFiles();
        for (var i = 0; i < layoutsFiles.length; i++) {
            var layoutFile = layoutsFiles[i];
            if (layoutFile.isDirectory()) {
                // this is not a layout, ignore
                continue;
            }

            layoutsData[layoutFile.getName()] = {
                path: layoutFile.getPath()
            }
        }
        return layoutsData;
    }

    /**
     * Returns unit's data.
     * @param unitsDir {string} path to the units directory e.g. "/app/units"
     * @return {{
     *          units: Object<string, {name: string, path: string, definition: Object}>,
     *          pushedUnits: Object<string, string[]>,
     *          extendedUnits: Object.<string, string[]>}} unit data
     */
    function getUnitsData(unitsDir) {
        /**
         * @type {Object<string, {shortName: string, path: string, definition: Object}>}
         */
        var units = {};
        /**
         * @type {Object<string, string[]>}
         */
        var pushedUnits = {};
        /**
         * @type {Object<string, string[]>}
         */
        var extendedUnits = {};
        var unitDirs = new File(unitsDir).listFiles();
        for (var i = 0; i < unitDirs.length; i++) {
            var unitDir = unitDirs[i];
            if (!unitDir.isDirectory()) {
                // this is not an unit, ignore
                continue;
            }

            var unitFullName = unitDir.getName();
            var unitShortName = unitFullName.split(".").pop();
            var unitPath = unitsDir + "/" + unitFullName;
            // Unit's definition is read form the <unit_short_name>.json file.
            // If doesn't exits it will be an empty JSON.
            var unitDefinition = {};
            var definitionFile = new File(unitPath + "/" + unitShortName + ".json");
            if (definitionFile.isExists() && !definitionFile.isDirectory()) {
                unitDefinition = require(definitionFile.getPath());
            } else {
                log.warn("Unable to find a definition file for unit '" + unitFullName + "'");
            }
            units[unitFullName] = {
                shortName: unitShortName,
                path: unitPath,
                definition: unitDefinition
            };

            var uriPatterns = unitDefinition.scope;
            if (uriPatterns && Array.isArray(uriPatterns)) {
                for (var j = 0; j < uriPatterns.length; j++) {
                    var uriPattern = uriPatterns[j];
                    if (!pushedUnits[uriPattern]) {
                        pushedUnits[uriPattern] = [];
                    }
                    pushedUnits[uriPattern].push(unitFullName);
                }
            }

            var parentUnitName = unitDefinition.extends;
            if (parentUnitName) {
                if (!extendedUnits[parentUnitName]) {
                    extendedUnits[parentUnitName] = [];
                }
                extendedUnits[parentUnitName].push(unitFullName);
            }
        }
        return {units: units, pushedUnits: pushedUnits, extendedUnits: extendedUnits};
    }

    var unitData = getUnitsData(configs.constants.DIRECTORY_APP_UNITS);

    return {
        layouts: getLayoutsData(configs.constants.DIRECTORY_APP_LAYOUTS),
        units: unitData.units,
        pushedUnits: unitData.pushedUnits,
        extendedUnits: unitData.extendedUnits
    };
}

function getRandomId() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < 5; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }
    return text;
}