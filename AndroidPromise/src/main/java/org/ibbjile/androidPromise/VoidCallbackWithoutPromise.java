package org.ibbjile.androidPromise;

public interface VoidCallbackWithoutPromise<T> extends PromiseCallback{
    void run(T result);
}
