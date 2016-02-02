---
layout: page
title: Architecture
css: joint
---

## Model

Click on an element or a link to highlight

{% include diagram.html %}

### App
App is the deployable artifact.

### Component
This is the re-usable element. A component can contain muliple pages and muliple fragments.

### Page
Page owns a URL. Pages can push elements to Zones defined. It can also define zones in it's markup.

### Zone
Zone are the placeholders for HTML.

### Fragment
Fragment is a UI Template (collection of HTML elements) and a controller logic (back-end logic) that will be used to render the template.
It can also define CSS, JS and public resources (eg: fonts). 

### App contains Fragments 
Apps can contain Fragments directly. 
These fragments are not meant to be shared with other projects, But will get used in multiple places in the same App.

sample dir structure
{% highlight text %}
org.wso2.apim.uuf.store
└── units
    └── api-thumbnail
{% endhighlight %}


### Fragments contains Fragments
