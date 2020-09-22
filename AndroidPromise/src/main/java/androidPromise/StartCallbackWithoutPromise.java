package androidPromise;

public interface StartCallbackWithoutPromise<T> extends PromiseCallback {
    T run() throws Exception;
}

