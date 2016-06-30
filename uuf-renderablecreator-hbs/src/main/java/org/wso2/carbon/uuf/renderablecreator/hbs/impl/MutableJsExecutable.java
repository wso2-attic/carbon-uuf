package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MutableJsExecutable extends JSExecutable implements MutableExecutable {

    private final Lock readLock;
    private final Lock writeLock;

    public MutableJsExecutable(String scriptSource, ClassLoader componentClassLoader, String scriptPath,
                               String componentPath) {
        super(scriptSource, componentClassLoader, scriptPath, componentPath);
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
    public void reload(String scriptSource) {
        try {
            writeLock.lock();
            loadScript(scriptSource);
        } finally {
            writeLock.unlock();
        }
    }
}
