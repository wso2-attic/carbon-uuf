package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.artifact.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtilTest {
    @DataProvider
    public Object[][] pathProvider() {
        return new Object[][]{
                // illegal cases. we'll try to do best
                {"", ""},
                {"a/b/c", "a/b/c"},
                {"components", "components"},
                {"components/pages/c/d", "components/pages/c/d"},
                {"pages", "pages"},
                {"pages/b", "pages/b"},

                // inside a built app artifact
                {"app/components/pages", "app/components/pages"},
                {"/root/app/components/b/c/d", "app/components/b/c/d"},
                {"a/b/app/components", "app/components"},

                // inside a dev-time app
                {"root/app/pages/x", "app/pages/x"},
        };
    }


    @Test(dataProvider = "pathProvider")
    public void testEmptyPath(String originalPath, String relativePath) throws Exception {
        Path path = FileUtil.relativePath(Paths.get(originalPath));
        Assert.assertEquals(path.toString(), relativePath, "path should relativized.");
    }

}
