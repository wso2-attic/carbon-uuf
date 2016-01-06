var route;

(function () {
    var log = new Log("error-pages-router");
    var constants = require("constants.js").constants;
    var utils = require("utils.js").utils;

    /**
     *
     * @param status {number} HTTP status of the error
     * @param message {String} error message
     * @param request {Object} HTTP request
     * @param response {Object} HTTP response
     */
    route = function (status, message, request, response) {
        var appConfigurations = utils.getAppConfigurations();
        var errorPagesConfigs = appConfigurations[constants.APP_CONF_ERROR_PAGES];
        if (!errorPagesConfigs) {
            return;
        }
        var errorPageFullName = errorPagesConfigs[status] || errorPagesConfigs["default"];
        if (!errorPageFullName) {
            return;
        }

        var lookupTable = utils.getLookupTable(appConfigurations);
        var errorPage = lookupTable.pages[errorPageFullName];
        if (!errorPage) {
            log.warn("Error page '" + errorPageFullName
                     + " mentioned in application configuration file '" + constants.FILE_APP_CONF
                     + "' does not exists.");
            return;
        } else if (errorPage.disabled) {
            log.warn("Error page '" + errorPageFullName
                     + " mentioned in application configuration file '" + constants.FILE_APP_CONF
                     + "' is disabled.");
            return;
        }

        /** @type {RenderingContext} */
        var renderingContext = {
            app: {
                context: utils.getAppContext(request),
                conf: appConfigurations
            },
            uri: errorPage.definition[constants.PAGE_DEFINITION_URI],
            uriParams: {},
            user: utils.getCurrentUser()
        };
        var templateContext = {status: status, message: message};
        var renderer = require("dynamic-files-renderer.js").renderer;
        renderer.renderUiComponent(errorPage, templateContext, renderingContext, lookupTable,
                                   response);
    };
})();
