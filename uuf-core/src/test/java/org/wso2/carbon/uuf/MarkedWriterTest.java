package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.MarkedWriter;

import java.util.HashMap;
import java.util.Map;

public class MarkedWriterTest {

    @Test
    public void testWithoutMarkers() throws Exception {
        MarkedWriter markedWriter = new MarkedWriter();
        markedWriter.write("First line.\n");
        markedWriter.write("Second line.\n");
        markedWriter.write("Third line.\n");
        markedWriter.write("Final text.");
        Assert.assertEquals(markedWriter.toString(), "First line.\nSecond line.\nThird line.\nFinal text.");
    }

    @Test
    public void testWithMarkers() throws Exception {
        MarkedWriter markedWriter = new MarkedWriter();
        markedWriter.addMarker("m0");
        markedWriter.write("First line.\n");
        markedWriter.write("Second line.\n");
        markedWriter.addMarker("m1");
        markedWriter.addMarker("m2");
        markedWriter.write("Third line.\n");
        markedWriter.write("Forth line.\n");
        markedWriter.addMarker("m3");
        Map<String, String> markerValues = new HashMap<>(4);
        markerValues.put("m0", "Marker zero,");
        markerValues.put("m1", "Marker one,");
        markerValues.put("m2", "Marker two,");
        markerValues.put("m3", "Marker three");
        String output = markedWriter.toString(markerValues);
        String expectedOutout = "Marker zero,First line.\nSecond line.\nMarker one,Marker two,Third line.\nForth line.\nMarker three";
        Assert.assertEquals(output, expectedOutout);
    }
}
