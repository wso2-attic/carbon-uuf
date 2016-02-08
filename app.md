---
layout: page
title: App Structure
css: joint
---

## Model

Click on an element or a link to highlight

{% include diagram.html %}

### App
This is the deployable element.
Responsible for serving all the HTTP request in a given application context.

### Component
This is the re-usable element. There can be 0 or more such dependencies referred from the App.
The app will not contain them at the development time, but will be included at build time.

### Page
Page owns a URL (patten) and specifies the layout need to render that page.
And it contains the HTML, and back-end logic needed to render the page.
Page may also contain CSS, fonts and other resources need to render that specific page.

### Fragment
A fragment is a part of a HTML page that can be reused.
And it contains the HTML, and back-end logic that is needed to render it. 
This may also contain some css, front end js and other public resources (eg: fonts).

### App contains Fragments
Apps can contain Fragments directly. 
These fragments are not meant to be shared with other projects, But will get used in multiple places in the same App.

sample dir structure
{% highlight text %}
org.wso2.apim.uuf.store
└── fragments
    └── api-thumbnail
{% endhighlight %}


### Fragment contains Fragment
