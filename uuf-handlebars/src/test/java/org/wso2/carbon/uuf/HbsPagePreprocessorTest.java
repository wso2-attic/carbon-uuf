package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.HbsPagePreprocessor;

import java.util.Optional;

public class HbsPagePreprocessorTest {

    private static HbsPagePreprocessor createHbsPagePreprocessor(String pageTemplateContent) {
        return new HbsPagePreprocessor(new StringTemplateSource("<test-source>", pageTemplateContent));
    }

    @Test
    public void test() {
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
}