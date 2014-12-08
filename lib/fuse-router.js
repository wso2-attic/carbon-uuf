//public function declarations
var route;

(function () {

    //public
    /**
     * front controller entity point. acts as the main function.
     */
    route = function () {
        //lets assume URL looks like https://my.domain.com/app/{one}/{two}/{three}/{four}
        var uri = request.getRequestURI(); // = app/{one}/{two}/{three}/{four}
        var parts = splitFirst(uri);
        var path = parts.tail; // = /{one}/{two}/{three}/{four}
        var handled = false;

        parts = splitFirst(path);
        if (parts.head == 'public') { // {one} == 'public'
            parts = splitFirst(parts.tail);
            if (splitFirst(parts.tail).head == 'less') { // {three} == 'less'
                handled = renderLess(parts.head, parts.tail);  // renderLess({two},{three}/{four})
            } else {
                handled = renderStatic(parts.head, parts.tail);
            }
        } else {
            handled = renderPage(path);
        }

        if (!handled) {
            response.sendError(404, 'Requested resource not found');
        }

    };


    //private
    var log = new Log('fuse.router');

    var getMime = function (path) {
        var index = path.lastIndexOf('.') + 1;
        var knowMime = {
            'js': 'application/javascript',
            'html': 'text/html',
            'htm': 'text/html',
            'woff': 'application/x-font-woff'
        };
        var mime;
        if (index >= 0) {
            mime = knowMime[path.substr(index)];
        }
        return mime || 'text/plain';
    };

    /**
     * '/a/b/c/d' -> {'a','b/c/d'}
     * @param path URI part, should start with '/'
     * @returns {{head: string, tail: string}}
     */
    var splitFirst = function (path) {
        var firstSlashPos = path.indexOf('/', 1);
        var head = path.substring(1, firstSlashPos);
        var tail = path.substring(firstSlashPos);
        return {head: head, tail: tail};
    };

    /**
     * @param str
     * @param prefix
     * @returns {boolean} true iif str starts with prefix
     */
    var startsWith = function (str, prefix) {
        return (str.lastIndexOf(prefix, 0) === 0);
    };

    var renderStatic = function (unit, path) {
        log.debug('[' + requestId + '] for unit "' + unit + '" a request received for a static file "' + path + '"');
        var staticFile = fuse.getFile(unit, 'public' + path);
        if (staticFile.isExists() && !staticFile.isDirectory()) {
            response.addHeader('Content-type', getMime(path));
            response.addHeader('Cache-Control', 'public,max-age=12960000');
            staticFile.open('r');
            var stream = staticFile.getStream();
            print(stream);
            staticFile.close();
            return true;
        }
        return false;
    };

    var renderPage = function (path) {
        var layout = null;
        var mainUnit = null;
        var unitDefinitions = util.getUnitDefinitions();
        var matched = [];
        zones = {};
        for (var i = 0; i < unitDefinitions.length; i++) {
            var definition = unitDefinitions[i];
            if (util.isMatched(definition)) {
                matched.push(definition);
                var zoneDef = util.getZoneDefinition(definition.name);
                for (var j = 0; j < zoneDef.zones.length; j++) {
                    var zone = zoneDef.zones[j];
                    if (!zones[zone]) {
                        zones[zone] = [];
                    }
                    zones[zone].push(definition.name);
                }
                if (zoneDef.layout) {
                    if (layout == null) {
                        layout = zoneDef.layout;
                        mainUnit = definition.name;
                    } else {
                        log.warn('[' + requestId + '] multiple layouts ' + mainUnit + ':' + layout + ' vs ' + definition.name + ':' + zoneDef.layout);
                    }
                }
            }
        }

        for(zone in zones){
            fuse.cleanupAncestors(zones[zone]);
        }

        log.debug(
            '[' + requestId + '] request for "' + path + '" will be rendered using layout "' +
            layout + '" (defined in "' + mainUnit + '") and zones ' +
            stringify(zones));
        var handlebars = require('handlebars-helpers.js');

        var output = handlebars.Handlebars.compileFile(util.getLayoutPath(layout))({});
        response.addHeader('Content-type', 'text/html');
        print(output);
        return true;
    };

    function fileToString(path) {
    }

    /**
     * convert less file to css and print to output. add '?nocache=true' to force regenerate.
     * @param unit name of the unit
     * @param path the path to the less file relative to unit root (should start with slash)
     * @returns {boolean} is successfully rendered.
     */
    function renderLess(unit, path) {
        log.debug('[' + requestId + '] for unit "' + unit + '" a request received for a less file "' + path + '"');
        var cacheKey = '/tmp/cached_' + unit + path.replace(/[^\w\.-]/g, '_');
        var cachedCss = new File(cacheKey);

        //TODO: move this check to caller function ??
        if (fuseDebug || request.getParameter('nocache') == 'true' || !cachedCss.isExists()) {
            var parts = splitFirst(path);
            var lessPath = '/public/less' + parts.tail.replace(/\.css$/, '') + '.less';
            var lessFile = fuse.getFile(unit, lessPath);

            if (lessFile.isExists()) {
                var x = require('less-rhino-1.7.5.js');
                x.compile([lessFile.getPath(), cacheKey]);
                log.debug('[' + requestId + '] for unit "' + unit + '" request for "' + path + '" is cached as "' + cacheKey + '"');
            }
        }


        if (cachedCss.isExists()) {
            response.addHeader('Content-type', 'text/css');
            response.addHeader('Cache-Control', 'public,max-age=12960000');
            cachedCss.open('r');
            var stream = cachedCss.getStream();
            print(stream);
            cachedCss.close();
            return true;
        }
        return false;

    }


})();