package androidPromise;

public interface VoidCallbackWithoutPromise<T> {
    void run(T result) throws Exception;
}
