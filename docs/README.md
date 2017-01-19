# Basics

A UUF app is a collection of UUF components.

## UUF Components

Could handle a specific functionality entirely. Every aspect of the functionality such as,

* Rendering UI
* Business logic
* Calling OSGi services
* Data access ?

could be handled in a component.

For example a component handling Authentication of users could take care of rendering the login UI, and using services from a WSO2 Identity Server instace authenticating users and managing sessions. A UUF app can depend on such a component to add the functionality of user authentication to itself with the help of a few configurations.

A UUF component Consists of,

* Pages
* Fragments
* Layouts

### Pages

* Handles a specific requested url.
* Renders HTML using [handlebars]().
* Possibly accompanies a js file that handles the business logic regarding the request.
* Accompaniying js file could define the global context for the handlerbars in a page
* Uses fragments and Layouts.

### Fragments

* Parts of pages.
* Renders HTML using [handlebars]().
* Could contain client side js.
* Could be shared between pages and could occur in many places of a page.

### Layouts

* Provides a frame for a page to render itself.
* Uses *Zones* to define empty spaces that are filled in by pages.
* Could be shared between pages

## UUF Apps

Consists of,

* A theme
* UUF components

An app can define components of its own or use external components.

### Themes

A theme controls the overall look and feel of an UUF app.
Contains resources that are used in the client side like css, js and fonts.