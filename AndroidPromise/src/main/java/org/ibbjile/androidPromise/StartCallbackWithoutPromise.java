package org.ibbjile.androidPromise;

public interface StartCallbackWithoutPromise<T> extends PromiseCallback {
    T run() throws Exception;
}

