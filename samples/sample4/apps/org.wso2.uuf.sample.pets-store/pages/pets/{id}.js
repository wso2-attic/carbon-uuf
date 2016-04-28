//noinspection JSUnusedGlobalSymbols
var onRequest = function (context) {
    return {"name": context.uriParams.id, "tags": ['white', 'short-hair']};
};