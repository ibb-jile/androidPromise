package androidPromise;

public interface VoidCallback<T> extends PromiseCallback{
    void run(T result, Promise<T> promise) throws Exception;
}
