package androidPromise;

public interface ThenCallbackWithoutPromise<T_PREV, T_NEW> {
    T_NEW run(T_PREV result) throws Exception;
}
