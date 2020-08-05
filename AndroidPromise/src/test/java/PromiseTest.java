import org.ibbjile.androidPromise.Promise;
import org.ibbjile.androidPromise.ThenCallback;
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

        Promise.resolveIt("Hello")
                .done(value -> {future.complete(value);});

        assertEquals("Hello", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void chainingPromise() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = new CompletableFuture<>();

        Promise<String> promise = Promise.resolveIt("hello")
                .then((result) -> result + "_world")
                .then((result) -> result + "_people")
                .then((result) -> result + ":" + result.length());

        promise.done(future::complete);

        assertEquals("hello_world_people:18", future.get(1, TimeUnit.SECONDS));

        future = new CompletableFuture<>();

        Promise<String> promise2 = new Promise<>((result) -> result + "_people");

        promise = Promise.resolveIt("hello")
                .then((result, p) -> p.resolve(result + "_world"))
                .then(promise2)
                .then((result) -> result.length())
                .then((length) -> length.toString());

        promise.done(future::complete);

        assertEquals("18", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void changeTypeInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        Promise.resolveIt("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result.length())
                .done((Integer result) -> future.complete(result));

        assertEquals((Integer) 7, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void asyncInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];


        Promise.resolveIt("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((result) -> result.length())
                .done(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals((Integer) 3, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void errorInAsyncChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise.resolveIt("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((result) -> result.length())
                .fail(future::complete);

        delayedPromise[0].reject(new Exception("ufff"));

        assertTrue(future.get(1, TimeUnit.SECONDS) instanceof Exception);
    }

    @Test
    public void catchErrorWithReject() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        Promise.resolveIt("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result + "aaaaaaaaaaaa")
                .done((result, p) -> p.reject(new Exception("ouu")))
                .then((String result) ->(result + "bbbbbbbbbbbbbbbbbbbbbb").length())
                .done((Integer result) -> future.cancel(true))
                .fail(future::complete);

        assertTrue(future.get(1, TimeUnit.SECONDS) != null);
    }

    @Test
    public void catchErrorWithThrow() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        Promise.resolveIt("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result + "aaaaaaaaaaaa")
                .then((String result) -> {
                    Integer integer = Integer.parseInt(result);
                    return "aaa";
                })
                .then((String result) -> (result + "bbbbbbbbbbbbbbbbbbbbbb").length())
                .done((Integer result) -> future.cancel(true))
                .fail(future::complete);

        assertTrue(future.get(1, TimeUnit.SECONDS) != null);
    }

    @Test
    public void joinNewPromiseToChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise<String> promise2 = new Promise<>((ThenCallback<Integer,String>)(s, p) -> {
            p.resolve(s + "-promise2");
        });

        Promise.resolveIt("hello")
                .done((result, p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .then(promise2)
                .then(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals("3-promise2", future.get(1, TimeUnit.SECONDS));
    }
}