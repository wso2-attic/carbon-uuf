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

package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * This class lazily loads the 'mime-map.properties' file and maps file extension to mime type using the file.
 */
public class MimeMapper {

    private static volatile Properties MIME_MAP = null;
    private static final Logger log = LoggerFactory.getLogger(MimeMapper.class);

    private static Properties loadMimeMap() {
        Properties mimeMap = new Properties();
        String mimePropertyFileName = "mime-map.properties";
        InputStream inputStream = MimeMapper.class.getClassLoader().getResourceAsStream(mimePropertyFileName);
        if (inputStream == null) {
            throw new UUFException("Cannot find MIME types property file '" + mimePropertyFileName + "'");
        }
        try {
            mimeMap.load(inputStream);
        } catch (IllegalArgumentException e) {
            throw new MalformedConfigurationException(
                    "MIME types property file is '" + mimePropertyFileName + "' is invalid.", e);
        } catch (IOException e) {
            throw new UUFException("Cannot read MIME types property file '" + mimePropertyFileName + "'.", e);
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            log.warn("Could not close input stream of MIME types property file '" + mimePropertyFileName + "'.", e);
        }
        return mimeMap;
    }

    public static Optional<String> getMimeType(String extension) {
        if (MIME_MAP == null) {
            // Here, class object 'MimeMapper.class' is used as the synchronization lock
            // because 'getMimeType()' is the only is public method.
            synchronized (MimeMapper.class) {
                if (MIME_MAP == null) {
                    MIME_MAP = loadMimeMap();
                }
            }
        }
        return Optional.ofNullable(MIME_MAP.getProperty(extension));
    }
}
