import org.ibbjile.androidPromise.ExceptionCallback;
import org.ibbjile.androidPromise.Promise;
import org.ibbjile.androidPromise.ThenCallback;
import org.ibbjile.androidPromise.VoidCallback;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PromiseTest {
    @Test
    public void justResolve() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        Promise.resolveValue("Hello")
                .done(value -> {
                    future.complete(value);
                });

        assertEquals("Hello", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void catchInResolveIt() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        Promise.resolveValue((p) -> p.resolve(this.simulateException()))
                .done(value -> {
                    future.cancel(true);
                })
                .fail(((ExceptionCallback<Exception>) future::complete));

        assertTrue(future.get() instanceof Exception);

        final CompletableFuture<Exception> future1 = new CompletableFuture<>();

        Promise.resolveValue(() -> this.simulateException())
                .done(value -> {
                    future1.cancel(true);
                })
                .fail((e) -> future1.complete((Exception) e));

        assertTrue(future1.get() instanceof Exception);
    }

    private String simulateException() throws Exception {
        throw new Exception("test exception");
    }

    @Test
    public void chainingPromise() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = new CompletableFuture<>();

        Promise<String> promise = Promise.resolveValue("hello")
                .then((String result) -> result + "_world")
                .then((String result) -> result + "_people")
                .then((String result) -> result + ":" + result.length());

        promise.done(future::complete);

        assertEquals("hello_world_people:18", future.get(1, TimeUnit.SECONDS));

        future = new CompletableFuture<>();

        Promise<String> promise2 = new Promise<>((result) -> result + "_people");

        promise = Promise.resolveValue("hello")
                .then((result, p) -> p.resolve(result + "_world"))
                .then(promise2)
                .then((String result) -> result.length())
                .then((Integer length) -> length.toString());

        promise.done(future::complete);

        assertEquals("18", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void changeTypeInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        Promise.resolveValue("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result.length())
                .done((Integer result) -> future.complete(result));

        assertEquals((Integer) 7, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void asyncInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];


        Promise.resolveValue("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .done(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals((Integer) 3, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void errorInAsyncChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise.resolveValue("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .fail(e -> future.complete((Exception) e));

        delayedPromise[0].reject(new Exception("ufff"));

        assertTrue(future.get(1, TimeUnit.SECONDS) instanceof Exception);
    }

    @Test
    public void catchErrorWithReject() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        Promise.resolveValue("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result + "aaaaaaaaaaaa")
                .done((result, p) -> p.reject(new Exception("ouu")))
                .then((String result) -> (result + "bbbbbbbbbbbbbbbbbbbbbb").length())
                .done((Integer result) -> future.cancel(true))
                .fail(e -> future.complete((Exception) e));

        assertTrue(future.get(1, TimeUnit.SECONDS) != null);
    }

    @Test
    public void catchErrorWithThrow() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        Promise.resolveValue("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result + "aaaaaaaaaaaa")
                .then((String result) -> {
                    Integer integer = Integer.parseInt(result);
                    return "aaa";
                })
                .then((String result) -> (result + "bbbbbbbbbbbbbbbbbbbbbb").length())
                .done((Integer result) -> future.cancel(true))
                .<Exception>fail(e -> future.complete((Exception) e));

        assertTrue(future.get(1, TimeUnit.SECONDS) != null);
    }

    @Test
    public void joinNewPromiseToChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise<String> promise2 = new Promise<>((ThenCallback<Integer, String>) (s, p) -> {
            p.resolve(s + "-promise2");
        });

        Promise.resolveValue("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .then(promise2)
                .then((String res) -> anotherPromise(res))
                .then(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals("3-promise2AnotherPromise", future.get(1, TimeUnit.SECONDS));
    }

    private Promise<String> anotherPromise(String param) {
        return new Promise<>((p) -> {
            p.resolve(param + "AnotherPromise");
        });
    }

    @Test
    public void all() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        Promise p1 = new Promise((VoidCallback) (result, promise) -> {
        });
        Promise p2 = new Promise((VoidCallback) (result, promise) -> {
        });
        Promise p3 = new Promise((VoidCallback) (result, promise) -> {
        });


        Promise allPromise = Promise.all(new Promise[]{p1, p2, p3}).done((Void) -> future.complete(true));

        Assert.assertFalse(future.isDone());
        p1.resolve(1);
        Assert.assertFalse(future.isDone());
        p2.resolve(2);
        Assert.assertFalse(future.isDone());
        p3.resolve(3);

        future.get(5, TimeUnit.SECONDS);
        Assert.assertTrue(future.isDone());

    }
}