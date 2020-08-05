package org.ibbjile.androidPromise;

public  interface ThenCallbackWithoutPromise<IN, OUT> {
    OUT run(IN result) throws Exception;
}
