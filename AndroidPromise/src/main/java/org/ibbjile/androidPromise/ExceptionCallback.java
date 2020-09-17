package org.ibbjile.androidPromise;

public interface ExceptionCallback<T extends Throwable> {
    void fail(T e);
}
