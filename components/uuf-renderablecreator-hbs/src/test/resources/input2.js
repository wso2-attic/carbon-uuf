var input2 = {
    "web-app": {
        "servlet": [
            {
                "servlet-name": "test",
                "servlet-class": "org.test.Servlet",
                "init-param": {
                    "configGlossary:installationAt": "Philadelphia, PA",
                    "configGlossary:adminEmail": "abc@abc.com",
                    "configGlossary:poweredBy": "test",
                    "configGlossary:poweredByIcon": "/images/test.gif",
                    "configGlossary:staticPath": "/content/static",
                    "templateProcessorClass": "org.test.WysiwygTemplate",
                    "templateLoaderClass": "org.test.FilesTemplateLoader",
                    "templatePath": "templates",
                    "templateOverridePath": "",
                    "defaultListTemplate": "listTemplate.htm",
                    "defaultFileTemplate": "articleTemplate.htm",
                    "useJSP": false,
                    "jspListTemplate": "listTemplate.jsp",
                    "jspFileTemplate": "articleTemplate.jsp",
                    "cachePackageTagsTrack": 200,
                    "cachePackageTagsStore": 200,
                    "cachePackageTagsRefresh": 60,
                    "cacheTemplatesTrack": 100,
                    "cacheTemplatesStore": 50,
                    "cacheTemplatesRefresh": 15,
                    "cachePagesTrack": 200,
                    "cachePagesStore": 100,
                    "cachePagesRefresh": 10,
                    "cachePagesDirtyRead": 10,
                    "searchEngineListTemplate": "forSearchEnginesList.htm",
                    "searchEngineFileTemplate": "forSearchEngines.htm",
                    "searchEngineRobotsDb": "WEB-INF/robots.db",
                    "useDataStore": true,
                    "dataStoreClass": "org.test.SqlDataStore",
                    "redirectionClass": "org.test.SqlRedirection",
                    "dataStoreName": "test",
                    "dataStoreDriver": "com.microsoft.jdbc.sqlserver.SQLServerDriver",
                    "dataStoreUrl": "jdbc:microsoft:sqlserver://LOCALHOST:1433;DatabaseName=goon",
                    "dataStoreUser": "sa",
                    "dataStorePassword": "dataStoreTestQuery",
                    "dataStoreTestQuery": "SET NOCOUNT ON;select test='test';",
                    "dataStoreLogFile": "/usr/local/tomcat/logs/datastore.log",
                    "dataStoreInitConns": 10,
                    "dataStoreMaxConns": 100,
                    "dataStoreConnUsageLimit": 100,
                    "dataStoreLogLevel": "debug",
                    "maxUrlLength": 500
                }
            },
            {
                "servlet-name": "testEmail",
                "servlet-class": "org.test.EmailServlet",
                "init-param": {
                    "mailHost": "mail1",
                    "mailHostOverride": "mail2"
                }
            },
            {
                "servlet-name": "testAdmin",
                "servlet-class": "org.test.AdminServlet"
            },

            {
                "servlet-name": "fileServlet",
                "servlet-class": "org.test.FileServlet"
            },
            {
                "servlet-name": "testTools",
                "servlet-class": "org.test.cms.testToolsServlet",
                "init-param": {
                    "templatePath": "toolstemplates/",
                    "log": 1,
                    "logLocation": "/usr/local/tomcat/logs/testTools.log",
                    "logMaxSize": "",
                    "dataLog": 1,
                    "dataLogLocation": "/usr/local/tomcat/logs/dataLog.log",
                    "dataLogMaxSize": "",
                    "removePageCache": "/content/admin/remove?cache=pages&id=",
                    "removeTemplateCache": "/content/admin/remove?cache=templates&id=",
                    "fileTransferFolder": "/usr/local/tomcat/webapps/content/fileTransferFolder",
                    "lookInContext": 1,
                    "adminGroupID": 4,
                    "betaServer": true
                }
            }],
        "servlet-mapping": {
            "test": "/",
            "testEmail": "/testutil/aemail/*",
            "testAdmin": "/admin/*",
            "fileServlet": "/static/*",
            "testTools": "/tools/*"
        },

        "taglib": {
            "taglib-uri": "test.tld",
            "taglib-location": "/WEB-INF/tlds/test.tld"
        }
    }
};
