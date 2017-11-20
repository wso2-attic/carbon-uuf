# TOC
1. [Basics](#basics)
2. How to Write an App
3. Best Practices
4. [Reference Guide](ref_guide.md)

# Basics

## UUF Apps

A UUF app is a collection of UUF components to provide its functionality. Additionaly it could use Themes to control its look and feel.

## UUF Components

A UUF component handles a specific functionality entirely. Every aspect of the functionality such as,

* Rendering the UI
* Business logic
* Calling OSGi services
* Data access

could be handled in a component.

For example a component handling authentication of users could take care of rendering the login UI, talking to an authentication service and managing sessions. A UUF app can depend on such a component to add the functionality of user authentication to itself with the help of a few configurations.

A UUF component Consists of,

* Pages
* Fragments
* Layouts

### Pages

A Page Handles a specific requested url. It renders html possibly using a template and relevent data. Fragments and Layouts are used by pages in this process.

### Fragments

A Fragment represent a specific part of a Page. A Fragment renders html just like a page does.

Fragments are used by pages, to avoid duplication when rendering many similar data. (Eg: A list of people)
Multiple pages that render similar data could share Fragments.

### Layouts

A Layout provides a *frame* for a Page to render itself in.

Uses UUF constructs named *Zones* to define empty spaces that are filled in by pages. Multiple pages could share Layouts.

## Themes

A Theme controls the overall look and feel of an UUF app. It contains resources that are used in the client side like style sheets, client side scripts and fonts.
