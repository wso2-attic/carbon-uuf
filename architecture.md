---
layout: page
title: Architecture
css: joint
---

## Model

Click on an element or a link to highlight

{% include diagram.html %}

### App
This is the deployable element.

### Component
This is the re-usable element.

### Page
Page owns a URL. Otherwise, it's very similar to a Unit, and have similar structures.

### Zone
Zone are the placeholders for HTML.

### Unit
This is a collection of HTML elements ,and back-end logic that's needed to render it. 
This may also contain some css, front end js and other public resources (eg: fonts).

### App contains Units
Apps can contain Units directly. 
These units are not meant to be shared with other projects, But will get used in multiple places in the same App.

sample dir structure
{% highlight text %}
org.wso2.apim.uuf.store
└── units
    └── api-thumbnail
{% endhighlight %}


### Unit contains Unit
