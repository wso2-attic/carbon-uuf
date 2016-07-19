//noinspection JSUnusedGlobalSymbols
function onRequest(env) {
    return {"tags": getTags()};
}

function getTags() {
    return ['white', 'short-hair'];
}