/**
 *
 * @param request
 * @param lookUpTable
 * @param configs
 */
function registerHelpers(request, lookUpTable, configs) {
    var log = new Log("[handlebars-helpers]");
    var handlebarsEnvironment = require(configs.constants.LIBRARY_HANDLEBARS).Handlebars;
    /**
     * @type {{currentLayout: string, zones: Object.<string, string[]>}}
     */
    handlebarsEnvironment.renderingDataModel = {
        currentLayout: null,
        zones: {}
    };

    /**
     * Returns the context and Handlebars template of the specified unit.
     * @param unitModel {Object<string, {shortName: string, path: string, definition: Object}>}
     *     unit model
     * @param contextParams {{appName: string, unitFullName: string, unitParams: Object<string,
     *     string>, parentContext: Object, request: Object}} parameters for unit context
     * @return {{context: Object, template: Object}| null} context and Handlebars template of the
     *     unit or <code>null</code> if the template of the unit does not exists
     */
    handlebarsEnvironment.getUnitContextAndTemplate = function (unitModel, contextParams) {

        var unitFilePath = unitModel.path + "/" + unitModel.shortName;
        var unitTemplateFile = new File(unitFilePath + ".hbs");
        if (!unitTemplateFile.isExists() || unitTemplateFile.isDirectory()) {
            return null;
        }
        unitTemplateFile.open('r');
        // TODO: check whether trimming is necessary or not
        var content = unitTemplateFile.readAll().trim();
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
                    },
                    httpRequest: contextParams.request
                };
                unitContext = script.onRequest(jsContext);
            }
        }
        unitContext.app = {uri: appUri};
        unitContext.unit = {publicUri: unitPublicUri};

        return {context: unitContext, template: unitTemplate};
    };

    handlebarsEnvironment.registerHelper("layout", function (layoutName, options) {
        handlebarsEnvironment.renderingDataModel.currentLayout = layoutName;
        return "";
    });

    handlebarsEnvironment.registerHelper("zone", function (zoneName, options) {
        var htmlContent = new handlebarsEnvironment.SafeString(options.fn(this));
        var renderingDataModel = handlebarsEnvironment.renderingDataModel;
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
                return "<parent unit '" + parentUnitFullName + "' does not exists>";
            }

            var parentUnitContextParams = {
                appName: lookUpTable.appName,
                unitFullName: unitFullName, // unitFullName is used to create the unit's public URI
                unitParams: options.hash,
                parentContext: null,
                handlebars: handlebarsEnvironment,
                request: request
            };
            parentUnitData = handlebarsEnvironment.getUnitContextAndTemplate(parentUnitModel,
                                                                             parentUnitContextParams);
            if (!parentUnitData) {
                return "<template of the parent unit '" + parentUnitFullName + "' does not exists>";
            }
        }

        var unitContextParams = {
            appName: lookUpTable.appName,
            unitFullName: unitFullName,
            unitParams: options.hash,
            parentContext: (parentUnitFullName ? parentUnitData.context : null),
            handlebars: handlebarsEnvironment,
            request: request
        };
        var unitData = handlebarsEnvironment.getUnitContextAndTemplate(unitModel,
                                                                       unitContextParams);
        if (!unitData) {
            return "<template of unit '" + unitFullName + "' does not exists>";
        }

        var html = unitData.template(unitData.context);
        if (parentUnitFullName) {
            // if has, render parent
            html = parentUnitData.template(parentUnitData.context); // + html;
        }
        return new handlebarsEnvironment.SafeString(html);
    });

    handlebarsEnvironment.registerHelper("defineZone", function (zoneName, options) {
        var zoneBuffer = handlebarsEnvironment.renderingDataModel.zones[zoneName];
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