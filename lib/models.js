/**
 * Represent a shareable UI component.
 * @constructor
 */
function UIComponent() {
    /** @type {string} */
    this.fullName = null;
    /** @type {string} */
    this.shortName = null;
    /** @type {string} */
    this.type = null;
    /** @type {string} */
    this.path = null;
    /** @type {boolean} */
    this.disabled = false;
    /** @type {number} */
    this.index = 1000000;
    /** @type {string} */
    this.templateFilePath = null;
    /** @type {string} */
    this.scriptFilePath = null;
    /** @type {Object} */
    this.definition = null;
    /** @type {UIComponent[]} */
    this.parents = [];
    /** @type {UIComponent[]} */
    this.children = [];
}

/**
 * Compares this UIComponent with the specified 'other' UIComponent.
 * @param other {UIComponent} other UIComponent to be compared
 * @returns {number} if this > other then 1; if this < other then -1; if this == other then 0
 */
UIComponent.prototype.compareTo = function (other) {
    var deltaOfIndexes = (this.index - other.index);
    return (deltaOfIndexes < 0) ? +1 : ((deltaOfIndexes > 0) ? -1 : 0);
};

/**
 * Compares and returns whether this UIComponent equals to the specified 'other' UIComponent.
 * @param other {UIComponent} other UIComponent to be compared
 * @returns {boolean} if this == other then <code>true</code>; otherwise <code>false</code>
 */
UIComponent.prototype.equals = function (other) {
    return (this.fullName == other.fullName);
};

UIComponent.prototype.toString = function () {
    return this.fullName;
};

/**
 * Represent a layout of a page.
 * @param fullName {string} full name
 * @param path {string} file path
 * @constructor
 */
function Layout(fullName, path) {
    /** @type {string} */
    this.fullName = fullName;
    /** @type {string} */
    this.path = path;
}

Layout.prototype.toString = function () {
    return this.fullName;
};

/**
 * The lookup table.
 * @typedef {{layouts: Object.<string, Layout>, pages: Object.<string, UIComponent>, uriPagesMap:
 *     Object.<string, string>, units: Object.<string, UIComponent>, pushedUnits: Object.<string,
 *     string[]>, uiComponents: Object.<string, UIComponent>}} LookupTable
 */
