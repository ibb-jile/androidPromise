package org.ibbjile.androidPromise;

public interface StartCallback<OUT> {
    void run(Promise<Void, OUT> promise);
}

