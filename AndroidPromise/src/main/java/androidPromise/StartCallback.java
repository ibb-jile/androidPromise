package androidPromise;

public interface StartCallback<T> extends PromiseCallback {
    void run(Promise<T> promise) throws Exception;
}

