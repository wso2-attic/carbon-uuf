/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.core;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Placeholder;

import java.util.List;
import java.util.Optional;

public class ThemeTest {

    @Test
    public void testRenderTheme() {
        List<String> cssRelativePaths = ImmutableList.of("css/main-styles.css");
        List<String> headJsRelativePaths = ImmutableList.of("js/main-script.css");
        List<String> bodyJsRelativePaths = ImmutableList.of("js/last-script.css");
        Theme theme = new Theme("theme-name", cssRelativePaths, headJsRelativePaths, bodyJsRelativePaths, null);
        RequestLookup requestLookup = new RequestLookup("/context-path", null, null);
        theme.addPlaceHolderValues(requestLookup);

        Optional<String> css = requestLookup.getPlaceholderContent(Placeholder.css);
        Assert.assertTrue(css.isPresent());
        Assert.assertEquals(css.get(), "<link href=\"/context-path/public/themes/theme-name/" +
                cssRelativePaths.get(0) + "\" rel=\"stylesheet\" type=\"text/css\" />");

        Optional<String> headJs = requestLookup.getPlaceholderContent(Placeholder.headJs);
        Assert.assertTrue(headJs.isPresent());
        Assert.assertEquals(headJs.get(), "<script src=\"/context-path/public/themes/theme-name/" +
                headJsRelativePaths.get(0) + "\" type=\"text/javascript\"></script>");

        Optional<String> js = requestLookup.getPlaceholderContent(Placeholder.js);
        Assert.assertTrue(js.isPresent());
        Assert.assertEquals(js.get(), "<script src=\"/context-path/public/themes/theme-name/" +
                bodyJsRelativePaths.get(0) + "\" type=\"text/javascript\"></script>");
    }
}
