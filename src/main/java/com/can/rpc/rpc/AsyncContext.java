package com.can.rpc.rpc;

import io.netty.util.internal.InternalThreadLocalMap;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author ccc
 */
public class AsyncContext {

    public static ThreadLocal<Boolean> async = new ThreadLocal<>();

    static {
        async.set(Boolean.FALSE);
    }

    @SuppressWarnings("unchecked")
    public static <T>CompletableFuture<T> asyncCall(Callable<T> callable) {
        try {
            async.set(Boolean.TRUE);
            final T result = callable.call();
            return (CompletableFuture<T>) result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            async.set(Boolean.FALSE);
        }
        throw null;
    }
}
