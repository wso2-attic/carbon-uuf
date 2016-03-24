package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.PlaceholderWriter;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderWriterTest {

    @Test
    public void testWithoutPlaceholders() throws Exception {
        PlaceholderWriter placeholderWriter = new PlaceholderWriter();
        placeholderWriter.write("First line.\n");
        placeholderWriter.write("Second line.\n");
        placeholderWriter.write("Third line.\n");
        placeholderWriter.write("Final text.");
        Assert.assertEquals(placeholderWriter.toString(), "First line.\nSecond line.\nThird line.\nFinal text.");
    }

    @Test
    public void testWithPlaceholders() throws Exception {
        PlaceholderWriter placeholderWriter = new PlaceholderWriter();
        placeholderWriter.addPlaceholder("p0");
        placeholderWriter.write("First line.\n");
        placeholderWriter.write("Second line.\n");
        placeholderWriter.addPlaceholder("p1");
        placeholderWriter.addPlaceholder("p2");
        placeholderWriter.write("Third line.\n");
        placeholderWriter.write("Forth line.\n");
        placeholderWriter.addPlaceholder("p3");
        Map<String, String> placeholderValues = new HashMap<>(4);
        placeholderValues.put("p0", "Placeholder zero,");
        placeholderValues.put("p1", "Placeholder one,");
        placeholderValues.put("p2", "Placeholder two,");
        placeholderValues.put("p3", "Placeholder three");
        String output = placeholderWriter.toString(placeholderValues);
        String expectedOutput = "Placeholder zero,First line.\nSecond line.\nPlaceholder one,Placeholder two," +
                "Third line.\nForth line.\nPlaceholder three";
        Assert.assertEquals(output, expectedOutput);
    }
}
