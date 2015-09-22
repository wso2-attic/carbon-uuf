function StaticRouter() {
    var log = new Log("[static-file-router]");
    var knownMimeTypes = {
        // text
        txt: 'text/plain',
        html: 'text/html',
        htm: 'text/html',
        js: 'application/x-javascript',
        css: 'text/css',
        xml: 'application/xml',
        hbs: 'text/x-handlebars-template',
        // fonts
        woff: 'application/font-woff',
        otf: 'application/font-sfnt',
        ttf: 'application/font-sfnt',
        // images
        jpg: 'image/jpeg',
        jpeg: 'image/jpeg',
        png: 'image/png',
        bmp: 'image/bmp',
        gif: 'image/gif',
        svg: 'image/svg+xml'
    };

    /**
     * Splits a full file name to its name and extension.
     * @param fullFileName {string} file name to be split e.g. foo.txt
     * @return {{name: string, extension: string}} splited parts
     */
    function splitFileName(fullFileName) {
        var index = fullFileName.lastIndexOf(".");
        return {name: fullFileName.substr(0, index), extension: fullFileName.substr(index + 1)}
    }

    /**
     * Returns a file object that represent the specified file.
     * @param unitFullName {string} full name of the unit
     * @param resourceType {string} resource type
     * @param fileName {string} file name with extension
     * @param lookUpTable {Object} lookup table
     * @param configs {Object} configurations
     * @return {Object|null} if file exists a File object, otherwise <code>null</code>
     */
    function getFile(unitFullName, resourceType, fileName, lookUpTable, configs) {
        var unitModel = lookUpTable.units[unitFullName];
        if (!unitModel) {
            return null; // unit does not exists
        }
        var filePathRelativeToUnit = null;
        if (resourceType == "less") {
            filePathRelativeToUnit = configs.constants.DIRECTORY_APP_UNIT_PUBLIC + "/less/"
                                     + splitFileName(fileName).name + ".less";
        } else {
            filePathRelativeToUnit = configs.constants.DIRECTORY_APP_UNIT_PUBLIC + "/"
                                     + resourceType + "/" + fileName;
        }
        // search in this unit
        var filePath = configs.constants.DIRECTORY_APP_UNITS + "/" + unitFullName
                       + filePathRelativeToUnit;
        var file = new File(filePath);
        if (file.isExists() && !file.isDirectory()) {
            return file; // this unit has the file
        }
        // search in parent unit
        var parentUnitFullName = unitModel.definition["extends"];
        if (!parentUnitFullName) {
            return null; // not an extended unit, hence does not has a parent unit
        }
        filePath = configs.constants.DIRECTORY_APP_UNITS + "/" + parentUnitFullName
                   + filePathRelativeToUnit;
        file = new File(filePath);
        if (file.isExists() && !file.isDirectory()) {
            return file; // parent unit has the file
        }

        return null; // file does not exits
    }

    function isLastModifiedDateEquals(file, request) {
        return String(file.getLastModified()) == request.getHeader("If-Modified-Since");
    }

    /**
     * Returns the MIME type of the specified file.
     * @param path {string} file path or name
     * @return {string} MIME type of the specified file
     */
    function getMimeType(path) {
        var extension = splitFileName(path).extension;
        if (!extension || extension.length == 0) {
            return knownMimeTypes['txt'];
        }
        var mimeType = knownMimeTypes[extension];
        if (mimeType) {
            return mimeType;
        }
        mimeType = knownMimeTypes[extension.toLowerCase()];
        if (mimeType) {
            return mimeType;
        }
        return knownMimeTypes['txt'];
    }

    function getLessParser(configs) {
        var less = require(configs.constants.LIBRARY_LESS).less;
        // Adapted from https://github.com/less/less.js/blob/v1.7.5/lib/less/rhino.js#L89
        less.Parser.fileLoader = function (file, currentFileInfo, callback, env) {
            var href = file;

            if (currentFileInfo && currentFileInfo.currentDirectory && !/^\//.test(file)) {
                href = less.modules.path.join(currentFileInfo.currentDirectory, file);
            }
            var path = less.modules.path.dirname(href);
            var newFileInfo = {
                currentDirectory: path,
                filename: href
            };

            if (currentFileInfo) {
                newFileInfo.entryPath = currentFileInfo.entryPath;
                newFileInfo.rootpath = currentFileInfo.rootpath;
                newFileInfo.rootFilename = currentFileInfo.rootFilename;
                newFileInfo.relativeUrls = currentFileInfo.relativeUrls;
            } else {
                newFileInfo.entryPath = path;
                newFileInfo.rootpath = less.rootpath || path;
                newFileInfo.rootFilename = href;
                newFileInfo.relativeUrls = env.relativeUrls;
            }

            var j = file.lastIndexOf('/');
            if (newFileInfo.relativeUrls && !/^(?:[a-z-]+:|\/)/.test(file) && j != -1) {
                var relativeSubDirectory = file.slice(0, j + 1);
                // append (sub|sup) directory  path of imported file
                newFileInfo.rootpath = newFileInfo.rootpath + relativeSubDirectory;
            }

            var data = null;
            var f = new File(href);
            try {
                f.open('r');
                data = f.readAll();
            } catch (e) {
                callback({
                    type: 'File',
                    message: "Cannot read '" + href + "' file."
                });
                return;
            } finally {
                f.close();
            }

            try {
                callback(null, data, href, newFileInfo, {lastModified: 0});
            } catch (e) {
                callback(e, null, href);
            }
        };

        // TODO: implement a proper caching mechanism for 'less'
        return less;
    }

    /**
     * Process the specified LESS file and generate CSS and write to the specified response.
     * @param lessFile {Object} file object of the processing LESS file
     * @param unitFullName {string} fully qualified name of the unit
     * @param cssFileName {string} name of the CSS file
     * @param configs {Object} configurations
     * @param isCachingEnabled {boolean} whether caching enabled or not
     * @param response {Object} HTTP response to be served
     */
    function renderLess(lessFile, unitFullName, cssFileName, configs, isCachingEnabled, response) {
        // cached CSS file name pattern: {unitFullName}_{cssFileName}.css
        var cacheFilePath = configs.constants.DIRECTORY_CACHE + "/" + unitFullName + "_"
                            + cssFileName;
        var cachedFile = new File(cacheFilePath);
        if (isCachingEnabled && cachedFile.isExists()) {
            response.addHeader("Content-type", "text/css");
            response.addHeader("Cache-Control", "public,max-age=12960000");
            response.addHeader("Last-Modified", String(lessFile.getLastModified()));
            cachedFile.open('r');
            print(cachedFile.getStream());
            cachedFile.close();
            return;
        }

        var less = getLessParser(configs);
        // Adapted from https://github.com/less/less.js/blob/v1.7.5/lib/less/rhino.js#L149
        var options = {
            depends: false,
            compress: false,
            cleancss: false,
            max_line_len: -1.0,
            optimization: 1.0,
            silent: false,
            verbose: false,
            lint: false,
            paths: [],
            color: true,
            strictImports: false,
            rootpath: "",
            relativeUrls: false,
            ieCompat: true,
            strictMath: false,
            strictUnits: false,
            filename: lessFile.getPath()
        };
        var lessParser = less.Parser(options);

        lessFile.open('r');
        var lessCode = lessFile.readAll();
        lessFile.close();
        var callback = function (error, root) {
            if (error) {
                // something went wrong when processing the LESS file
                var errorMsg = stringify(error);
                log.warn("Failed to process '" + lessFile.getPath() + "' file due to " + errorMsg);
                response.sendError(500, "Could not process LESS file. \n" + errorMsg);
                return;
            }

            var result = root.toCSS(options);
            cachedFile.open('w');
            cachedFile.write(result);
            cachedFile.close();

            response.addHeader("Content-type", "text/css");
            response.addHeader("Cache-Control", "public,max-age=12960000");
            response.addHeader("Last-Modified", String(lessFile.getLastModified()));
            print(result);
        };
        var globalVars = {"unit-class": splitFileName(unitFullName).extension}; // unit's short name
        lessParser.parse(lessCode, callback, {globalVars: globalVars});
    }

    /**
     * Process a HTTP request that requests a static file.
     * @param request {Object} HTTP request to be processed
     * @param response {Object} HTTP response to be served
     * @param lookUpTable {Object}
     * @param configs {Object} configurations
     */
    this.route = function (request, response, lookUpTable, configs) {
        // URI = /{appName}/public/{unitFullName}/{resourceType}/{fileName}.{extension}
        var uri = request.getRequestURI();
        var parts = uri.split("/");
        // parts= ["/", {appName}, "public", {unitFullName}, {resourceType}, {fileName}.{extension}]
        if (parts.length != 6) {
            // an invalid URI
            log.warn("Request URI '" + uri + "' is invalid.");
            response.sendError(400, "Malformed URL");
            return;
        }

        var requestedFile = getFile(parts[3], parts[4], parts[5], lookUpTable, configs);
        if (!requestedFile) {
            // this file either does not exists or it is a directory
            var filePath = configs.constants.DIRECTORY_APP_UNITS + "/" + parts[3]
                           + configs.constants.DIRECTORY_APP_UNIT_PUBLIC + "/" + parts[4] + "/"
                           + parts[5];
            log.warn("Cannot find requested '" + filePath + "' file.");
            response.sendError(404, "Requested resource not found");
            return;
        }

        var isCachingEnabled = !(request.getParameter("nocache") == "true");
        if (isCachingEnabled && isLastModifiedDateEquals(requestedFile, request)) {
            // requested file file has not changed since last serve
            response.status = 304;
            return;
        }

        var resourceType = parts[4];
        if (resourceType == "less") {
            // process less and return css
            renderLess(requestedFile, parts[3], parts[5], configs, isCachingEnabled, response);
        } else {
            response.addHeader("Content-type", getMimeType(parts[5]));
            response.addHeader("Cache-Control", "public,max-age=12960000");
            response.addHeader("Last-Modified", String(requestedFile.getLastModified()));
            requestedFile.open('r');
            print(requestedFile.getStream());
            requestedFile.close();
        }
    };
}
