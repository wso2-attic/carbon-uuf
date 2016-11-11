/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.uuf.handlebars;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.js.JsFunctionsImpl;
import org.wso2.carbon.uuf.spi.HttpRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for testing javascript functions used by the script engine in UUF.
 *
 * @since 1.0.0
 */
public class JsFunctionsTest {

    @DataProvider
    public Object[][] sendToClientJS() {
        return new Object[][]{
                {"a", "{\"a\" : \"b\"}", "{\\\"a\\\" : \\\"b\\\"}"},
                {"abc", "{\n" +
                        "    \"glossary\": {\n" +
                        "        \"title\": \"example glossary\",\n" +
                        "\t\t\"GlossDiv\": {\n" +
                        "            \"title\": \"S\",\n" +
                        "\t\t\t\"GlossList\": {\n" +
                        "                \"GlossEntry\": {\n" +
                        "                    \"ID\": \"SGML\",\n" +
                        "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
                        "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
                        "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
                        "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
                        "\t\t\t\t\t\"GlossDef\": {\n" +
                        "                        \"para\": \"A meta-markup language.\",\n" +
                        "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
                        "                    },\n" +
                        "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}",
                        "{\\n    " +
                                "\\\"glossary\\\": {\\n        " +
                                "\\\"title\\\": \\\"example glossary\\\",\\n\\t\\t\\\"GlossDiv\\\": {\\n            " +
                                "\\\"title\\\": \\\"S\\\",\\n\\t\\t\\t\\\"GlossList\\\": {\\n                " +
                                "\\\"GlossEntry\\\": {\\n                    \\\"ID\\\": \\\"SGML\\\",\\n\\t\\t\\t\\t" +
                                "\\t\\\"SortAs\\\": \\\"SGML\\\",\\n\\t\\t\\t\\t\\t\\\"GlossTerm\\\": " +
                                "\\\"Standard Generalized Markup Language\\\",\\n\\t\\t\\t\\t\\t\\\"Acronym\\\": " +
                                "\\\"SGML\\\",\\n\\t\\t\\t\\t\\t\\\"Abbrev\\\": \\\"ISO 8879:1986\\\"," +
                                "\\n\\t\\t\\t\\t\\t\\\"GlossDef\\\": {\\n                        \\\"para\\\": " +
                                "\\\"A meta-markup language.\\\",\\n\\t\\t\\t\\t\\t\\t\\\"GlossSeeAlso\\\": " +
                                "[\\\"GML\\\", \\\"XML\\\"]\\n                    }," +
                                "\\n\\t\\t\\t\\t\\t\\\"GlossSee\\\":" +
                                " \\\"markup\\\"\\n                }\\n            }\\n        }\\n    " +
                                "}\\n}"
                },
                {"name", "{\n" +
                        "        \"name\": \"Nipuna Dashboard\",\n" +
                        "        \"id\": \"nipuna-dashboard\",\n" +
                        "        \"version\": \"1.0.2\",\n" +
                        "        \"description\": \"\",\n" +
                        "        \"content\": {\n" +
                        "            \"hideAllMenuItems\": false,\n" +
                        "            \"banner\": {\n" +
                        "                \"customBannerExists\": false,\n" +
                        "                \"globalBannerExists\": false\n" +
                        "            },\n" +
                        "            \"pages\": [{\n" +
                        "                \"id\": \"page0\",\n" +
                        "                \"title\": \"Page 0\",\n" +
                        "                \"views\": {\n" +
                        "                    \"content\": {\n" +
                        "                        \"default\": {\n" +
                        "                            \"blocks\": [{\n" +
                        "                                \"height\": 3,\n" +
                        "                                \"id\": \"a\",\n" +
                        "                                \"width\": 12,\n" +
                        "                                \"x\": 0,\n" +
                        "                                \"y\": 0\n" +
                        "                            }, {\n" +
                        "                                \"height\": 3,\n" +
                        "                                \"id\": \"b\",\n" +
                        "                                \"width\": 12,\n" +
                        "                                \"x\": 0,\n" +
                        "                                \"y\": 3\n" +
                        "                            }, {\n" +
                        "                                \"height\": 3,\n" +
                        "                                \"id\": \"c\",\n" +
                        "                                \"width\": 12,\n" +
                        "                                \"x\": 0,\n" +
                        "                                \"y\": 6\n" +
                        "                            }],\n" +
                        "                            \"name\": \"Default View\",\n" +
                        "                            \"roles\": [\"Internal/Everyone\", \"admin\"]\n" +
                        "                        }\n" +
                        "                    },\n" +
                        "                    \"fluidLayout\": false\n" +
                        "                }\n" +
                        "            }],\n" +
                        "            \"menu\": [{\n" +
                        "                \"id\": \"page0\",\n" +
                        "                \"isHidden\": false,\n" +
                        "                \"subordinates\": [],\n" +
                        "                \"title\": \"Page 0\"\n" +
                        "            }]\n" +
                        "        },\n" +
                        "        \"theme\": {\n" +
                        "            \"name\": \"Default Theme\",\n" +
                        "            \"properties\": {\n" +
                        "                \"lightDark\": \"dark\",\n" +
                        "                \"showSideBar\": false\n" +
                        "            }\n" +
                        "        },\n" +
                        "        \"isCustomizable\": false,\n" +
                        "        \"isSharable\": false,\n" +
                        "        \"isAnon\": false,\n" +
                        "        \"apiAuth\": {\n" +
                        "            \"accessTokenUrl\": \"\",\n" +
                        "            \"apiKey\": \"\",\n" +
                        "            \"apiSecret\": \"\",\n" +
                        "            \"identityServerUrl\": \"\"\n" +
                        "        },\n" +
                        "        \"permission\": {\n" +
                        "            \"editor\": [],\n" +
                        "            \"viewer\": [],\n" +
                        "            \"owner\": []\n" +
                        "        }\n" +
                        "    }",
                        "{\\n        " +
                                "\\\"name\\\": \\\"Nipuna Dashboard\\\",\\n        \\\"id\\\": " +
                                "\\\"nipuna-dashboard\\\",\\n        \\\"version\\\": \\\"1.0.2\\\",\\n        " +
                                "\\\"description\\\": \\\"\\\",\\n        \\\"content\\\": {\\n            " +
                                "\\\"hideAllMenuItems\\\": false,\\n            \\\"banner\\\": {\\n                " +
                                "\\\"customBannerExists\\\": false,\\n                \\\"globalBannerExists\\\": " +
                                "false\\n            },\\n            \\\"pages\\\": [{\\n                " +
                                "\\\"id\\\": \\\"page0\\\",\\n                \\\"title\\\": \\\"Page 0\\\",\\n " +
                                "               \\\"views\\\": {\\n                    \\\"content\\\": {\\n " +
                                "                       \\\"default\\\": {\\n                            " +
                                "\\\"blocks\\\": [{\\n                                \\\"height\\\": 3,\\n   " +
                                "                             \\\"id\\\": \\\"a\\\",\\n                  " +
                                "              \\\"width\\\": 12,\\n                                \\\"x\\\": 0,\\n" +
                                "                                \\\"y\\\": 0\\n                            }, {\\n" +
                                "                                \\\"height\\\": 3,\\n " +
                                "                               \\\"id\\\": \\\"b\\\",\\n  " +
                                "                              \\\"width\\\": 12,\\n  " +
                                "                              \\\"x\\\": 0,\\n " +
                                "                               \\\"y\\\": 3\\n                            }, {\\n " +
                                "                               \\\"height\\\": 3,\\n                          " +
                                "      \\\"id\\\": \\\"c\\\",\\n                                \\\"width\\\": 12,\\n" +
                                "                                \\\"x\\\": 0,\\n                               " +
                                " \\\"y\\\": 6\\n                            }],\\n                           " +
                                " \\\"name\\\": \\\"Default View\\\",\\n                            " +
                                "\\\"roles\\\": [\\\"Internal/Everyone\\\", \\\"admin\\\"]\\n                    " +
                                "    }\\n                    },\\n                    \\\"fluidLayout\\\": false\\n " +
                                "               }\\n            }],\\n            \\\"menu\\\": [{\\n            " +
                                "    \\\"id\\\": \\\"page0\\\",\\n                \\\"isHidden\\\": false,\\n     " +
                                "           \\\"subordinates\\\": [],\\n                " +
                                "\\\"title\\\": \\\"Page 0\\\"\\n            }]\\n        },\\n       " +
                                " \\\"theme\\\": {\\n            \\\"name\\\": \\\"Default Theme\\\",\\n       " +
                                "     \\\"properties\\\": {\\n                \\\"lightDark\\\": \\\"dark\\\",\\n  " +
                                "              \\\"showSideBar\\\": false\\n            }\\n        },\\n     " +
                                "   \\\"isCustomizable\\\": false,\\n        \\\"isSharable\\\": false,\\n    " +
                                "    \\\"isAnon\\\": false,\\n        \\\"apiAuth\\\": {\\n       " +
                                "     \\\"accessTokenUrl\\\": \\\"\\\",\\n            \\\"apiKey\\\": \\\"\\\",\\n  " +
                                "          \\\"apiSecret\\\": \\\"\\\",\\n          " +
                                "  \\\"identityServerUrl\\\": \\\"\\\"\\n        },\\n     " +
                                "   \\\"permission\\\": {\\n            \\\"editor\\\": [],\\n          " +
                                "  \\\"viewer\\\": [],\\n            \\\"owner\\\": []\\n        }\\n    " +
                                "}"
                }
        };
    }


    private static API createAPI() {
        API api = mock(API.class);
        when(api.getRequestLookup()).thenReturn(new RequestLookup("/contextPath", mock(HttpRequest.class), null));
        return api;
    }


    @Test(dataProvider = "sendToClientJS")
    public void testSendToClient(String varName, String inputJS, String outputJS) {
        API api = createAPI();
        JsFunctionsImpl jsFunctions = new JsFunctionsImpl(api);
        jsFunctions.getSendToClientFunction().call(varName, inputJS);
        Assert.assertEquals(api.getRequestLookup().getPlaceholderContent(Placeholder.js).get(),
                "<script type=\"text/javascript\">var " + varName + "=\"" + outputJS + "\";</script>");
    }
}
