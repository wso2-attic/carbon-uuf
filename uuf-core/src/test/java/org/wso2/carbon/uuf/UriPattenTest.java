package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.UriPatten;

import static java.lang.Integer.signum;

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
                {"/ab", "/a{x}"},
                {"/ab", "/{x}b"},
        };
    }

    @DataProvider
    public Object[][] uriProvider() {
        return new Object[][]{
                {"/", "/"},
                {"/a", "/a"},
                {"/{a}", "/b"},
                {"/{a}b", "/xb"},
                {"/x{a}o/y{b}o", "/xpo/ypo"},
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

    @Test
    public void testInvariants() throws Exception {
        UriPatten[] pattens = new UriPatten[]{
                new UriPatten("/"),
                new UriPatten("/a"),
                new UriPatten("/ab"),
                new UriPatten("/a{b}"),
                new UriPatten("/{a}/{b}"),
                new UriPatten("/{a}"),
                new UriPatten("/{a}"),
                new UriPatten("/a/{b}"),
                new UriPatten("/ab/{b}"),
                new UriPatten("/{a}/b"),
        };
        //following invariants are specified in java.lang.Comparable
        for (int x = 0; x < pattens.length; x++) {
            for (int y = x + 1; y < pattens.length; y++) {
                int xy = pattens[x].compareTo(pattens[y]);
                int yx = pattens[y].compareTo(pattens[x]);
                Assert.assertTrue(signum(xy) == -signum(yx));
                for (int z = y + 1; z < pattens.length; z++) {
                    int xz = pattens[x].compareTo(pattens[z]);
                    int yz = pattens[y].compareTo(pattens[z]);
                    if (xy > 0 && yz > 0) {
                        Assert.assertTrue(xz > 0);
                    } else if (yx > 0 && yz > 0) {
                        Assert.assertTrue(yz > 0);
                    } else if (xy == 0) {
                        Assert.assertTrue(signum(xz) == signum(yz));
                    }
                }
            }
        }
    }

}
