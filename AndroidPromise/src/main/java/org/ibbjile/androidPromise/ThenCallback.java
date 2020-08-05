package org.ibbjile.androidPromise;

public  interface ThenCallback<IN, OUT> {
    void run(IN result, Promise<IN, OUT> promise);
}

