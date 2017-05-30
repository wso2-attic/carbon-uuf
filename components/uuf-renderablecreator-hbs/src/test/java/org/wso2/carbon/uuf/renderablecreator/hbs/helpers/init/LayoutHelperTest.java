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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.renderablecreator.hbs.exception.HbsRenderableCreationException;

import java.util.Optional;

import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init.InitHelpersTestUtil.createHbsPagePreprocessor;

/**
 * Test cases for {@code {{layout}}} helper.
 *
 * @since 1.0.0
 */
public class LayoutHelperTest {

    @Test
    public void test() {
        String pageTemplateContent = "foo\nbar\n{{layout \"test-layout\"}}bla bla\nfoobar";
        Optional<String> layoutName = createHbsPagePreprocessor(pageTemplateContent).getLayoutName();

        Assert.assertEquals(layoutName.orElse(null), "test-layout");
    }

    @Test
    public void testSettingMultiple() {
        String pageTemplateContent = "foo\nbar\n{{layout \"layout-1\"}}bla bla\n{{layout \"layout-2\"}}\nfoobar";
        Assert.assertThrows(HbsRenderableCreationException.class, () -> createHbsPagePreprocessor(pageTemplateContent));
    }
}
