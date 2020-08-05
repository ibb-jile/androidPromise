package org.ibbjile.androidPromise;

public interface ThenCallbackWithoutPromise<T_PREV, T_NEW> extends PromiseCallback {
    T_NEW run(T_PREV result) throws Exception;
}
