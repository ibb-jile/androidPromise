package org.ibbjile.androidPromise;

public interface PromiseInPromiseCallback<T_PREV, T_NEW> extends PromiseCallback {
    Promise<T_NEW> run(T_PREV result) throws Exception;
}
