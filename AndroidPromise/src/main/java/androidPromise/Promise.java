package androidPromise;

public class Promise<T> {

    private PromiseCallback thenCallback;
    private ExceptionCallback errorCallback;
    private T result;
    private Exception exeption;
    private PromiseState state;
    private Promise child;

    public static <T> Promise<T> resolveValue(T value) {
        return new Promise<>(promise -> {
            promise.resolve(value);
        });
    }

    public static <T> Promise<T> resolveValue(StartCallback<T> callback) {
        return new Promise<>(callback);
    }

    public static <T> Promise<T> resolveValue(StartCallbackWithoutPromise<T> callback) {
        return resolveValue(promise -> promise.resolve(callback.run()));
    }

    public static Promise<Void> all(Promise[] promises) {
        return new Promise<>(promise -> {
            for (Promise p : promises) {
                p.done((result, ppp) -> {
                    boolean done = true;
                    for (Promise pp : promises) {
                        if (pp.state != PromiseState.Finished) {
                            done = false;
                            break;
                        }
                    }
                    if (done) {
                        promise.resolve(null);
                    }
                });
            }
        });
    }

    public Promise(StartCallback<T> callback) {
        this.init(callback);
        this.run(null);
    }

    public <T_PREV, T_NEW> Promise(ThenCallback<T_PREV, T_NEW> callback) {
        this.init(callback);
    }

    public <T_PREV, T_NEW> Promise(ThenCallbackWithoutPromise<T_PREV, T_NEW> thenCallback) {
        this.init((ThenCallback<T_PREV, T_NEW>) (result, promise) -> {
            try {
                promise.resolve(thenCallback.run(result));
            } catch (Exception e) {
                promise.reject(e);
            }
        });
    }

    public Promise(VoidCallback<T> callback) {
        this.init(callback);
    }

    public Promise<T> done(VoidCallback<T> thenCallback) {
        this.child = new Promise<>(thenCallback);
        this.continueInChain();

        return this.child;
    }

    public Promise<T> done(VoidCallbackWithoutPromise<T> thenCallback) {
        this.child = new Promise<>((VoidCallback<T>) (result, promise) -> {
            try {
                thenCallback.run(result);
                promise.resolve(result);
            } catch (Exception exception) {
                promise.reject(exception);
            }
        });
        this.continueInChain();

        return this.child;
    }

    public <T_NEW> Promise<T_NEW> then(PromiseInPromiseCallback<T, T_NEW> thenCallback) {
        this.child = new Promise<T_NEW>((ThenCallback<T, T_NEW>) (result, promise) -> thenCallback.run(result).done((res) -> promise.resolve(res)));
        this.continueInChain();

        return this.child;
    }

    public <T_NEW> Promise<T_NEW> then(ThenCallback<T, T_NEW> thenCallback) {
        this.child = new Promise<T_NEW>(thenCallback);
        this.continueInChain();

        return this.child;
    }

    public <T_NEW> Promise<T_NEW> then(ThenCallbackWithoutPromise<T, T_NEW> thenCallback) {
        this.child = new Promise<T_NEW>((ThenCallback<T, T_NEW>) (result, promise) -> {
            try {
                promise.resolve(thenCallback.run(result));
            } catch (Exception exception) {
                promise.reject(exception);
            }
        });
        this.continueInChain();

        return this.child;
    }

    public <T_NEW> Promise<T_NEW> then(Promise<T_NEW> newPromise) {
        this.child = newPromise;
        this.continueInChain();

        return this.child;
    }

    public <T extends Throwable> void fail(ExceptionCallback<T> errorCallback) {
        this.errorCallback = errorCallback;
        this.invokeFail();
    }

    public void resolve(T result) {
        this.result = result;
        this.setState(PromiseState.Finished);
        this.invokeThen();
    }

    public void reject(Exception exception) {
        this.exeption = exception;
        this.setState(PromiseState.Failed);
        this.invokeFail();
    }

    private void setState(PromiseState state) {
        if (this.state != PromiseState.Finished) {
            this.state = state;
        }
    }

    private void throwException(Exception exeption) {
        this.exeption = exeption;
        this.state = PromiseState.Failed;
        this.invokeFail();
    }

    private void init(PromiseCallback callback) {
        this.thenCallback = callback;
        this.setState(PromiseState.Initialized);
    }

    private void run(Object resultFromPrev) {
        this.setState(PromiseState.Running);
        try {
            if (this.thenCallback != null) {
                if (this.thenCallback instanceof VoidCallback) {
                    ((VoidCallback) this.thenCallback).run(resultFromPrev, this);
                } else if (this.thenCallback instanceof ThenCallback) {
                    ((ThenCallback) this.thenCallback).run(resultFromPrev, this);
                } else if (this.thenCallback instanceof StartCallback) {
                    ((StartCallback) this.thenCallback).run(this);
                } else if (this.thenCallback instanceof PromiseInPromiseCallback) {
                    ((PromiseInPromiseCallback) this.thenCallback).run(resultFromPrev);
                }
            }
        } catch (Exception ex) {
            this.reject(ex);
        }
    }

    private void continueInChain() {
        if (this.state == PromiseState.Failed) {
            this.invokeFail();
        } else {
            this.invokeThen();
        }
    }

    private void invokeFail() {
        if (this.state == PromiseState.Failed) {
            if (this.errorCallback != null) {
                this.errorCallback.fail(this.exeption);
            } else if (this.child != null) {
                this.child.throwException(this.exeption);
            }
        }
    }

    private void invokeThen() {
        if (this.state == PromiseState.Finished && this.child != null) {
            this.child.run(this.result);
        }
    }

    enum PromiseState {
        Initialized,
        Running,
        Finished,
        Failed
    }
}

