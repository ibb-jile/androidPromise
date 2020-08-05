package org.ibbjile.androidPromise;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PromiseTest {
    @Test
    public void simplePromise() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        Promise.resolveIt("Hello").then(future::complete);

        assertEquals("Hello", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void chainPromise() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();


        Promise.resolveIt("hello")
                .then((String result) -> {
                    System.out.println(result);
                    return result + "_world";
                })
                .then((String result) -> result + ":" + result.length())
                .then(future::complete);

        assertEquals("hello_world:11", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void changeTypeInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        Promise.resolveIt("hello")
                .then((String result) -> result + ":" + result.length())
                .then((String result) -> result.length())
                .then((Integer result) -> future.complete(result));

        assertEquals((Integer) 7, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void asyncInChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];


        Promise.resolveIt("hello")
                .then((String result, Promise<String, String> p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .then(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals((Integer) 3, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void errorInAsyncChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Exception> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise.resolveIt("hello")
                .then((String result, Promise<String, String> p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
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
                .then((String result, Promise<String, String> p) -> p.reject(new Exception("ouu")))
                .then((String result, Promise<String, Integer> p) -> p.resolve((result + "bbbbbbbbbbbbbbbbbbbbbb").length()))
                .then((Integer result) -> future.cancel(true))
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
                .then((String result, Promise<String, Integer> p) -> p.resolve((result + "bbbbbbbbbbbbbbbbbbbbbb").length()))
                .then((Integer result) -> future.cancel(true))
                .fail(future::complete);

        assertTrue(future.get(1, TimeUnit.SECONDS) != null);
    }

    @Test
    public void joinNewPromiseToChain() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<String> future = new CompletableFuture<>();

        final Promise[] delayedPromise = new Promise[1];

        Promise<Integer, String> promise2 = new Promise<>((s, p) -> {
            p.resolve(s + "-promise2");
        });

        Promise.resolveIt("hello")
                .then((String result, Promise<String, String> p) -> delayedPromise[0] = p)
                .then((String result) -> result.length())
                .then(promise2)
                .then(future::complete);

        delayedPromise[0].resolve("hey");

        assertEquals("3-promise2", future.get(1, TimeUnit.SECONDS));
    }
}