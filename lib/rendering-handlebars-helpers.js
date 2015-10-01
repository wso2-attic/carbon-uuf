/**
 *
 * @param renderingDataModel {{appName: string, currentLayout: string, zones: Object.<string,
 *     string[]>}}
 * @param lookUpTable
 * @param configs
 */
function registerHelpers(renderingDataModel, lookUpTable, configs) {
    var log = new Log("[handlebars-helpers]");
    var handlebarsEnvironment = require(configs.constants.LIBRARY_HANDLEBARS).Handlebars;

    /**
     * Returns the context and Handlebars template of the specified unit.
     * @param unitModel {Object<string, {shortName: string, path: string, definition: Object}>}
     *     unit model
     * @param contextParams {{appName: string, unitFullName: string, unitParams: Object<string,
     *     string>, parentContext: Object}} parameters for unit context
     * @return {{context: Object, template: Object}| null} context and Handlebars template of the
     *     unit or <code>null</code> if the template of the unit does not exists
     */
    function getUnitContextAndTemplate(unitModel, contextParams) {

        var unitFilePath = unitModel.path + "/" + unitModel.shortName;
        var unitTemplateFile = new File(unitFilePath + ".hbs");
        if (!unitTemplateFile.isExists() || unitTemplateFile.isDirectory()) {
            return null;
        }
        unitTemplateFile.open('r');
        var content = unitTemplateFile.readAll();
        unitTemplateFile.close();
        // TODO: implement a proper caching mechanism for 'unitTemplate'
        var unitTemplate = handlebarsEnvironment.compile(content);

        var appUri = "/" + contextParams.appName;
        var unitPublicUri = "/" + contextParams.appName + "/public/" + contextParams.unitFullName;
        var unitContext = {};
        var unitJsFile = new File(unitFilePath + ".js");
        if (unitJsFile.isExists()) {
            var script = require(unitJsFile.getPath());
            if (script.hasOwnProperty('onRequest')) {
                var jsContext = {
                    app: {
                        name: contextParams.appName,
                        uri: appUri
                    },
                    unit: {
                        params: contextParams.unitParams,
                        publicUri: unitPublicUri,
                        parentContext: contextParams.parentContext
                    }
                };
                unitContext = script.onRequest(jsContext);
            }
        }
        unitContext.app = {uri: appUri};
        unitContext.unit = {publicUri: unitPublicUri};

        return {context: unitContext, template: unitTemplate};
    }

    handlebarsEnvironment.registerHelper("layout", function (layoutName, options) {
        if (renderingDataModel.currentLayout) {
            log.warn("Layout is already set to '" + renderingDataModel.currentLayout + "'.");
        }
        renderingDataModel.currentLayout = layoutName;
        return "";
    });

    handlebarsEnvironment.registerHelper("zone", function (zoneName, options) {
        var htmlContent = new handlebarsEnvironment.SafeString(options.fn(this));
        var isOverride = options.hash["override"];
        if (!renderingDataModel.zones[zoneName] || (isOverride && isOverride == "true")) {
            renderingDataModel.zones[zoneName] = [];
        }
        renderingDataModel.zones[zoneName].push(htmlContent);
        return "";
    });

    handlebarsEnvironment.registerHelper("unit", function (unitFullName, options) {
        /** @type {Object<string, {shortName: string, path: string, definition: Object}>} */
        var unitModel = lookUpTable.units[unitFullName];
        if (!unitModel) {
            return "<unit '" + unitFullName + "' does not exists>";
        }

        var parentUnitFullName = unitModel.definition["extends"];
        var parentUnitData = null;
        if (parentUnitFullName) {
            /** @type {Object<string, {shortName: string, path: string, definition: Object}>} */
            var parentUnitModel = lookUpTable.units[parentUnitFullName];
            if (!parentUnitModel) {
                var msg = "Parent unit '" + parentUnitFullName + "' of unit '" + unitFullName
                          + "' does not exists.";
                log.error(msg);
                throw new Error(msg);
            }

            var parentUnitContextParams = {
                appName: renderingDataModel.appName,
                unitFullName: unitFullName, // unitFullName is used to create the unit's public URI
                unitParams: options.hash,
                parentContext: null,
                handlebars: handlebarsEnvironment
            };
            parentUnitData = getUnitContextAndTemplate(parentUnitModel, parentUnitContextParams);
            if (!parentUnitData) {
                var msg = "Template '.hbs' file of unit '" + parentUnitFullName
                          + "' does not exists.";
                log.error(msg);
                throw new Error(msg);
            }
        }

        var unitContextParams = {
            appName: renderingDataModel.appName,
            unitFullName: unitFullName,
            unitParams: options.hash,
            parentContext: (parentUnitFullName ? parentUnitData.context : null),
            handlebars: handlebarsEnvironment
        };
        var unitData = getUnitContextAndTemplate(unitModel, unitContextParams);
        if (!unitData) {
            var msg = "Template '.hbs' file of unit '" + unitFullName + "' does not exists.";
            log.error(msg);
            throw new Error(msg);
        }

        var html = unitData.template(unitData.context);
        if (parentUnitFullName) {
            // if has, render parent
            html = parentUnitData.template(parentUnitData.context); // + html;
        }
        return new handlebarsEnvironment.SafeString(html);
    });

    handlebarsEnvironment.registerHelper("defineZone", function (zoneName, options) {
        var zoneBuffer = renderingDataModel.zones[zoneName];
        if (zoneBuffer) {
            return (new handlebarsEnvironment.SafeString(zoneBuffer.join("")));
        }
        if (options.fn) {
            // render what is inside {{#defineZone "zoneName"}} ... {{/defineZone}}
            return new handlebarsEnvironment.SafeString(options.fn(this));
        }
        return "";
    });

    return handlebarsEnvironment;
}