package androidPromise;

public  interface ThenCallback<T_PREV, T_NEW> extends PromiseCallback {
    void run(T_PREV result, Promise<T_NEW> promise) throws Exception;
}

