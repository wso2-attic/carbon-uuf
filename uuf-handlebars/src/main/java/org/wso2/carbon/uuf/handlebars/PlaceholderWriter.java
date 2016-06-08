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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlaceholderWriter extends Writer {

    private List<Object> buffers;
    private StringBuilder currentBuffer;

    public PlaceholderWriter() {
        buffers = new ArrayList<>();
        currentBuffer = new StringBuilder();
        buffers.add(currentBuffer);
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
    public void close() {
        currentBuffer = null;
        buffers = null;
    }

    public void addPlaceholder(String placeholderName) {
        addPlaceholder(placeholderName, null);
    }

    public void addPlaceholder(String placeholderName, String defaultContent) {
        buffers.add(new PlaceholderMarker(placeholderName, defaultContent));
        currentBuffer = new StringBuilder();
        buffers.add(currentBuffer);
    }

    public String toString(Map<String, String> placeholderValues) {
        StringBuilder output = new StringBuilder();
        for (Object item : buffers) {
            if (item instanceof PlaceholderMarker) {
                // This is a marked placeholder.
                PlaceholderMarker marker = (PlaceholderMarker) item;
                String placeholderValue = placeholderValues.get(marker.getName());
                output.append((placeholderValue == null) ? marker.getDefaultContent().orElse("") : placeholderValue);
            } else {
                // This is a normal string buffer.
                output.append(item);
            }
        }
        return output.toString();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Object item : buffers) {
            if (item instanceof PlaceholderMarker) {
                // This is a marked placeholder.
                PlaceholderMarker marker = (PlaceholderMarker) item;
                output.append(marker.getDefaultContent().orElse(null));
            } else {
                // This is a normal string buffer.
                output.append(item);
            }
        }
        return output.toString();
    }

    private class PlaceholderMarker {
        private final String name;
        private final String defaultContent;

        public PlaceholderMarker(String name) {
            this(name, null);
        }

        public PlaceholderMarker(String name, String defaultContent) {
            this.name = name;
            this.defaultContent = defaultContent;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getDefaultContent() {
            return Optional.ofNullable(defaultContent);
        }
    }
}
