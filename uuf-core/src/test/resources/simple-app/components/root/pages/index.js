function onRequest(context){
    var msg = "my message";
    print(msg);
    return {msg: msg};
}