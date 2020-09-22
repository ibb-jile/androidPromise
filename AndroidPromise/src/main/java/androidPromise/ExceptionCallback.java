package androidPromise;

public interface ExceptionCallback<T extends Throwable> {
    void fail(T e);
}
