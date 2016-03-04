package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.UriPatten;

public class UriPattenTest {

    @DataProvider
    public Object[][] pattenProvider() {
        return new Object[][]{
                {"/a", "/{a}"},
                {"/a/b", "/{a}/b"},
                {"/a/b", "/a/{b}"},
                {"/a/b/", "/a/{b}/"},
                {"/a/b", "/{a}/{b}"},
                {"/a/b/", "/{a}/{b}/"},
                {"/{a}/b", "/{a}/{b}"},
                {"/a/{b}", "/{a}/{b}"},
        };
    }

    @DataProvider
    public Object[][] uriProvider() {
        return new Object[][]{
                {"/", "/"},
                {"/a", "/a"},
                {"/{a}", "/b"},
        };
    }

    @Test(dataProvider = "pattenProvider")
    public void testOrdering(String a, String b) throws Exception {
        UriPatten aPatten = new UriPatten(a);
        UriPatten bPatten = new UriPatten(b);
        int i = aPatten.compareTo(bPatten);
        int j = bPatten.compareTo(aPatten);
        Assert.assertTrue(i < 0, a + " should be more specific than " + b);
        Assert.assertTrue(j > 0, a + " should be more specific than " + b);
    }

    @Test(dataProvider = "uriProvider")
    public void testMatching(String a, String b) throws Exception {
        Assert.assertTrue(new UriPatten(a).match(b));
    }
}
