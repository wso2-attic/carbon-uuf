/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;

public class HbsPreprocessorTest {

    private static HbsPreprocessor createHbsPagePreprocessor(String pageTemplateContent) {
        return new HbsPreprocessor(new StringTemplateSource("<test-source>", pageTemplateContent));
    }

    @Test
    public void testLayout() {
        String pageTemplateContent = "foo\nbar\n{{layout \"test-layout\"}}bla bla\nfoobar";
        Optional<String> layoutName = createHbsPagePreprocessor(pageTemplateContent).getLayoutName();
        Assert.assertTrue(layoutName.isPresent(), "This page has a layout");
        Assert.assertEquals(layoutName.get(), "test-layout");
    }

    @Test
    public void testMultipleLayouts() {
        String pageTemplateContent = "foo\nbar\n{{layout \"layout-1\"}}bla bla\n{{layout \"layout-2\"}}\nfoobar";
        try {
            Optional<String> layoutName = createHbsPagePreprocessor(pageTemplateContent).getLayoutName();
            Assert.fail("Multiple layouts for the same page is not allowed.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 4, "error is in the 4th line");
            Assert.assertEquals(error.column, 2, "error is in the 2nd column");
        }
    }

    @Test
    public void testSecured() {
        String templateContent = "foo\nbar\n{{secured}}bla bla\nfoobar";
        boolean isSecured = createHbsPagePreprocessor(templateContent).isSecured();
        Assert.assertTrue(isSecured, "This page/fragment is secured");
    }
}