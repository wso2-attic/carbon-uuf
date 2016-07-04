/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MutableJsExecutable extends JSExecutable implements MutableExecutable {

    private final Lock readLock;
    private final Lock writeLock;

    public MutableJsExecutable(String scriptSource, ClassLoader componentClassLoader, String absolutePath,
                               String relativePath, String componentPath) {
        super(scriptSource, componentClassLoader, absolutePath, relativePath, componentPath);
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    @Override
    public Object execute(Object context, API api) {
        try {
            readLock.lock();
            return super.execute(context, api);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getPath() {
        return super.getPath();
    }

    @Override
    public void reload(String scriptSource) {
        try {
            writeLock.lock();
            compile(scriptSource);
        } finally {
            writeLock.unlock();
        }
    }
}
