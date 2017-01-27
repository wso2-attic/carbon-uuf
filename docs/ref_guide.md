# UUF Reference Guide

# TOC
1. [Javascript Globals](#javascript-globals)
2. [Handlerbars Helpers](helpers.md)
3. Configuration
4. Maven Plugin

# Javascript Globals

## `callOSGiService()`

**`callOSGiService(serviceClassName, serviceMethodName, methodParameters[])`**

Calls a back end OSGi service.

```javascript
function onGet(env) {
    var result = callOSGiService("org.wso2.carbon.uuf.sample.hello.service.HelloService", "getHelloMessage", ["John"]);
    return {textFromHelloService: result};
}
```

Then the result can be used in templates.

```html
<div>
    Hello Text: <span>{{textFromHelloService}}</span>
<div>
```

## `getOSGiServices()`

**`getOSGiServices(serviceInterfaceName)`**

Returns a map of class names and instances of OSGi services that implement a given OSGi service interface.

```javascript
function onGet(env) {
    // get services that implement 'HelloService' interface
    var services = getOSGiServices("org.wso2.carbon.uuf.sample.hello.HelloService");

    // access one of the imlementations in the map
    var implService = services['org.wso2.carbon.uuf.sample.hello.internal.impl.HelloService'];

    var newResult = implService.getHelloMessage("John");
    return {textFromHelloService: result};
}
```

## `callMicroService()`
 > Not implemented

## `sendError()`

**`sendError(statusCode, message)`**

Immediately halts execution of the request handler and responses with the given http error status code.

```javascript
function onGet(env) {
    sendError(501, "This request is not supported at the moment.");
};
```

## `sendRedirect()`

**`sendError(url)`**

Immediately halts execution of the request handler and redirects request to the given url. The redirection used is HTTP 302 (Moved temporarily).

```javascript
function onGet(env) {
    sendRedirect("http://www.google.lk");
};
```

## `Log`

Provides following methods that can be used to do logging.

```javascript
function onGet(env) {
    Log.debug("Debug message"); // Logs a string on DEBUG level
    Log.debug({info: "Debug info", code: 500}); // Logs an object on DEBUG level
    
    Log.error("Debug message"); // Logs a string on ERROR level
    
    Log.info("Debug message"); // Logs a string on INFO level
    
    Log.trace("Debug message"); // Logs a string on TRACE level
    
    Log.warn("Debug message"); // Logs a string on WARN level
};
```