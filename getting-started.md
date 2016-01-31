---
layout: page
title: Getting Started
---

## Cereating an App

Go the the directory you want to create the UUF app and run following maven command
{% highlight bash %}
mvn archetype:generate -DarchetypeCatalog=local -DarchetypeGroupId=org.wso2.carbon.ui.uuf.maven.tools -DarchetypeArtifactId=uuf-app-creator -DarchetypeVersion=1.0-SNAPSHOT -DgroupId=org.wso2.carbon.project -DartifactId=appname -DinteractiveMode=false
{% endhighlight %}
