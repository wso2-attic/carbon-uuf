//noinspection JSUnusedGlobalSymbols
function onRequest(context) {
    return {"tags": getTags(context.pathParams['id'])};
}

function getTags(petName) {
    return ['white', 'short-hair'];
}