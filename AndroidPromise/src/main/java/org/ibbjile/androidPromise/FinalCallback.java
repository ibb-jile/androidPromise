package org.ibbjile.androidPromise;

public interface FinalCallback<T> {
    void onComplete(T result);
}
