package org.ibbjile.androidPromise;

public interface VoidCallback<T> extends PromiseCallback{
    void run(T result, Promise<T> promise);
}
