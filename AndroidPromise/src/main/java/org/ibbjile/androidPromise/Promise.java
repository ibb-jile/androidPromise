package org.ibbjile.androidPromise;

public class Promise<IN, OUT> {

    private ThenCallback<IN, OUT> thenCallback;
    private ExceptionCallback errorCallback;
    private OUT result;
    private Exception exeption;
    private PromiseState state;
    private Promise child;

    public static <OUT> Promise<Void, OUT> resolveIt(OUT value) {
        return new Promise<>((Promise<Void, OUT> p) -> p.resolve(value));
    }

    public Promise(StartCallback<OUT> startCallback) {
        this.setState(PromiseState.Initialized);
        this.setState(PromiseState.Running);
        startCallback.run((Promise<Void, OUT>) this);
    }

    public <NEWIN, NEWOUT> Promise(ThenCallback<NEWIN, NEWOUT> thenCallback) {
        this.thenCallback = (ThenCallback<IN, OUT>) thenCallback;
    }

    public <NEWOUT> Promise<OUT, NEWOUT> then(ThenCallback<OUT, NEWOUT> continuable) {
        this.child = new Promise<OUT, NEWOUT>(continuable);
        this.continueInChain();

        return this.child;
    }

    public <NEWOUT> Promise<OUT, NEWOUT> then(ThenCallbackWithoutPromise<OUT, NEWOUT> continuable) {
        this.child = new Promise<OUT, NEWOUT>((ThenCallback<OUT, NEWOUT>) (result, promise) -> {
            try {
                promise.resolve(continuable.run(result));
            } catch (Exception exception) {
                promise.reject(exception);
            }
        });
        this.continueInChain();

        return this.child;
    }

    public <NEWOUT> Promise<OUT, NEWOUT> then(Promise<OUT, NEWOUT> newPromise) {
        this.child = newPromise;
        this.continueInChain();

        return this.child;
    }

    public void fail(ExceptionCallback errorCallback) {
        this.errorCallback = errorCallback;
        this.invokeFail();
    }

    public void resolve(OUT result) {
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

    private void run(IN resultFromPrev) {
        if (this.thenCallback != null) {
            this.thenCallback.run(resultFromPrev, this);
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

