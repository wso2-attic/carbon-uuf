# Handlebars Helpers

Following helpers can be used in handlebars within UUF Pages and Fragments.

##`js`

**`{{js "path/to/js/file"}}`**

Fills the js placeholder in the layout with a script tag in the html page linking to the javascript file in the given path. The path is relative to the `public` directory of the App or Component.

Example: If your component structure is like following,
```
org.wso2.carbon.uuf.sample
└── src
    └── main
        ├── fragments
        ├── layouts
        │   └── main.hbs
        ├── pages
        │   └── index.hbs
        └── public
            └── js
                └── test.js
```

To include `test.js` in your `index.hbs` file you can do,

`{{js "js/test.js"}}`

Placeholder in your `main.hbs` layout should be defined by,

`{{placeholder "js"}}`

##`css`

**`{{css "path/to/css/file"}}`**

Fills the css placeholder in the layout with a script tag in the html page linking to the css file in the given path. The path is relative to the `public` directory of the App or Component.

Example: If your component structure is like following,
```
org.wso2.carbon.uuf.sample
└── src
    └── main
        ├── fragments
        ├── layouts
        │   └── main.hbs
        ├── pages
        │   └── index.hbs
        └── public
            └── css
                └── test.css
```

To include `test.css` in your `index.hbs` file you can do,

`{{css "css/test.css"}}`

Placeholder in your `main.hbs` layout should be defined by,

`{{placeholder "css"}}`

##`favicon`

**`{{favicon "images/favicon.png" type="image/png"}}`**

Fills the favicon placeholder in the layout with a link tag referencing image given in the path. So browsers will display the given image as the favicon. The path is relative to the `public` directory of the App or Component.

Example: If your component structure is like following,
```
org.wso2.carbon.uuf.sample
└── src
    └── main
        ├── fragments
        ├── layouts
        │   └── main.hbs
        ├── pages
        │   └── index.hbs
        └── public
            └── images
                └── favicon.png
```

To serve `favicon.png` as your favicon in your `index.hbs` file you can do,

`{{favicon "images/favicon.png" type="image/png"}}`

Placeholder in your `main.hbs` layout should be defined by,

`{{placeholder "favicon"}}`

To avoid defining favicon in each of your pages you can define a default value for the favicon in your layouts.

```html
{{#placeholder "favicon"}}
    <link rel="shortcut icon" href="{{public "images/default-favicon.png"}}" type="image/png" />
{{/placeholder}}
```