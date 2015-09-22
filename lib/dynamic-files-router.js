function DynamicRouter() {

    var log = new Log("[dynamic-file-router]");

    function getHandlebarsEnvironment(request, lookUpTable, configs) {
        return (require("rendering-handlebars-helpers.js")).registerHelpers(request, lookUpTable, configs);
    }

    /**
     * Returns a File object to the specified page.
     * @param pageUri {string} page path requested in the URL
     * @param pagesDirPath {string} path of the directory where pages are stored
     * @return {File} requested page file
     */
    function getPageFile(pageUri, pagesDirPath) {
        /*
         * PAGE URI                REAL PATH
         * /foo/bar         /app/pages/foo/bar.hbs OR /app/pages/foo/bar/index.hbs
         * /foo/bar/        /app/pages/foo/bar.hbs OR /app/pages/foo/bar/index.hbs
         */
        if (pageUri[pageUri.length - 1] == "/") {
            // remove last slash, now its like "/foo/bar"
            pageUri = pageUri.substring(0, pageUri.length - 1);
        }
        // /app/pages/foo/bar.hbs
        var pageFile = new File(pagesDirPath + "/" + pageUri + ".hbs");
        if (pageFile.isExists()) {
            return pageFile;
        }
        // /app/pages/foo/bar/index.hbs
        pageFile = new File(pagesDirPath + "/" + pageUri + "/index.hbs");
        if (pageFile.isExists()) {
            return pageFile;
        }
        // page not found 404
        return null;
    }

    function renderPushedUnits(pageUri, lookUpTable, handlebarsEnvironment) {
        var uriMatcher = new URIMatcher(pageUri);
        /**
         * @type {Object.<string, string[]>}
         */
        var allPushedUnits = lookUpTable.pushedUnits;
        var uriPatterns = Object.keys(allPushedUnits);
        var pushedUnitsHbs = "";
        for (var i = 0; i < uriPatterns.length; i++) {
            var uriPattern = uriPatterns[i];
            if (uriMatcher.match(uriPattern)) {
                pushedUnitsHbs +=
                    '{{unit "' + allPushedUnits[uriPattern].join('"}}{{unit "') + '" }}';
            }
        }
        // TODO: implement a proper caching mechanism for 'pushedUnitsTemplate'
        var pushedUnitsTemplate = handlebarsEnvironment.compile(pushedUnitsHbs);
        pushedUnitsTemplate({});
    }

    function renderPage(pageFile, handlebarsEnvironment) {
        pageFile.open('r');
        // TODO: check whether trimming is necessary or not
        var content = pageFile.readAll().trim();
        pageFile.close();
        // TODO: implement a proper caching mechanism for 'pageTemplate'
        var pageTemplate = handlebarsEnvironment.compile(content);
        pageTemplate({});
    }

    function renderLayout(handlebarsEnvironment, configs, response) {
        var layoutFile = new File(configs.constants.DIRECTORY_APP_LAYOUTS + "/"
                                  + handlebarsEnvironment.renderingDataModel.currentLayout + ".hbs");
        layoutFile.open('r');
        // TODO: check whether trimming is necessary or not
        var content = layoutFile.readAll().trim();
        layoutFile.close();
        // TODO: implement a proper caching mechanism for 'layoutTemplate'
        var layoutTemplate = handlebarsEnvironment.compile(content);

        response.addHeader("Content-type", "text/html");
        print(layoutTemplate({}));
    }

    this.route = function (request, response, lookUpTable, configs) {
        // lets assume URL looks like https://my.domain.com/app/{one}/{two}/{three}/{four}
        var uri = request.getRequestURI(); // = /app/{one}/{two}/{three}/{four}
        var positionOfSecondSlash = uri.indexOf("/", 1);
        var pageUri = uri.substring(positionOfSecondSlash); // /{one}/{two}/{three}/{four}
        var pageFile = getPageFile(pageUri, configs.constants.DIRECTORY_APP_PAGES);
        if (!pageFile) {
            response.sendError(404, "Requested page not found");
            return;
        }

        lookUpTable.appName = uri.substring(1, positionOfSecondSlash);

        var handlebarsEnvironment = getHandlebarsEnvironment(request, lookUpTable, configs);
        renderPushedUnits(uri, lookUpTable, handlebarsEnvironment);
        renderPage(pageFile, handlebarsEnvironment);
        renderLayout(handlebarsEnvironment, configs, response);
    };

}
