//public function declarations
var getFile, getAncestors, toRelativePath, cleanupAncestors, getUnitPath;

(function () {
    //private
    var log = new Log('fuse.core');
    var lookUpTable = null;

    getAncestors = function (unit) {
        var definitions = util.getUnitDefinitions();
        var extendedFrom;
        var ancestors = [unit];

        if (lookUpTable === null) {
            lookUpTable = {};
            for (var i = 0; i < definitions.length; i++) {
                var definition = definitions[i];
                lookUpTable[definition.name] = i;
            }
        }

        extendedFrom = definitions[lookUpTable[unit]].definition.extends;
        while (extendedFrom) {
            ancestors.push(extendedFrom);
            extendedFrom = definitions[lookUpTable[extendedFrom]].definition.extends;
        }

        return ancestors;

    };


    //public
    getUnitPath = function (unit) {
        return '/units/' + unit;
    };

    cleanupAncestors = function (units) {
        var toDelete = {};
        var len = units.length;
        for (var i = 0; i < len; i++) {
            var unit = units[i];
            if (!toDelete[unit]) {
                var ancestors = getAncestors(unit);
                for (var j = 1; j < ancestors.length; j++) {
                    toDelete[ancestors[j]] = unit;
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
     * @param unit name of the unit
     * @param path path relative to unit root.
     * @param opt_suffix
     * @returns {File}
     */
    getFile = function (unit, path, opt_suffix) {
        var slashPath = ((path[0] === '/') ? '' : '/') + path;
        var selfFileName = '';
        var fileName = '';
        if (opt_suffix) {
            selfFileName = unit + opt_suffix;
            slashPath = slashPath + ((slashPath[slashPath.length - 1] === '/') ? '' : '/');
        }

        var selfFile = new File(getUnitPath(unit) + slashPath + selfFileName);
        if (selfFile.isExists()) {
            log.debug(
                '[' + requestId + '] for unit "' + unit + '" file resolved : "'
                + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
            );

            return selfFile;
        }

        var ancestors = getAncestors(unit);
        for (var i = 1; i < ancestors.length; i++) {
            var ancestor = ancestors[i];
            if (opt_suffix) {
                fileName = ancestor + opt_suffix;
            }
            var file = new File(getUnitPath(ancestor) + slashPath + fileName);
            if (file.isExists()) {
                log.debug(
                    '[' + requestId + '] for unit "' + unit + '" file resolved : "'
                    + slashPath + selfFileName + '" -> "' + file.getPath() + '"'
                );
                return file;
            }
        }
        log.debug(
            '[' + requestId + '] for unit "' + unit + '" (non-excising) file resolved : "'
            + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
        );
        return selfFile;
    };

})();
