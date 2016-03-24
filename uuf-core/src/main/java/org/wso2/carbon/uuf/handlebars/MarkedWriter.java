package org.wso2.carbon.uuf.handlebars;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarkedWriter extends Writer {

    private List<StringBuilder> buffers;
    private StringBuilder currentBuffer;
    private List<String> markers;

    public MarkedWriter() {
        buffers = new ArrayList<>();
        currentBuffer = new StringBuilder();
        buffers.add(currentBuffer);
        markers = new ArrayList<>();
    }

    @Override
    public Writer append(final char c) throws IOException {
        currentBuffer.append(c);
        return this;
    }

    @Override
    public Writer append(final CharSequence csq) throws IOException {
        currentBuffer.append(csq);
        return this;
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        currentBuffer.append(csq, start, end);
        return this;
    }

    @Override
    public void write(final char[] buffer) throws IOException {
        currentBuffer.append(buffer);
    }

    @Override
    public void write(final int c) throws IOException {
        currentBuffer.append((char) c);
    }

    @Override
    public void write(final String str) throws IOException {
        currentBuffer.append(str);
    }

    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        currentBuffer.append(str, off, len);
    }

    @Override
    public void write(final char[] buffer, final int off, final int len) throws IOException {
        currentBuffer.append(buffer, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        currentBuffer = null;
        buffers = null;
        markers = null;
    }

    public void addMarker(final String markerName) {
        currentBuffer = new StringBuilder();
        buffers.add(currentBuffer);
        markers.add(markerName);
    }

    public String toString(Map<String, String> markerValues) {
        StringBuilder tmpBuffer = new StringBuilder();
        tmpBuffer.append(buffers.get(0));
        for (int i = 0; i < markers.size(); i++) {
            String markerValue = markerValues.get(markers.get(i));
            tmpBuffer.append(markerValue);
            tmpBuffer.append(buffers.get(i + 1));
        }
        return tmpBuffer.toString();
    }

    @Override
    public String toString() {
        StringBuilder tmpBuffer = new StringBuilder();
        for (StringBuilder buffer : buffers) {
            tmpBuffer.append(buffer);
        }
        return tmpBuffer.toString();
    }
}
