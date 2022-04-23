package com.can.rpc.rpc.context;

import com.can.rpc.rpc.SyncResult;
import com.can.rpc.rpc.protocol.cprotocol.FutureAdapter;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author ccc
 */
public class AsyncContext {

    public static ThreadLocal<Boolean> async = new ThreadLocal<>();

    public static ThreadLocal<CompletableFuture<SyncResult>> asyncFuture = new ThreadLocal<>();

    public static void setAsyncFuture(CompletableFuture<SyncResult> future) {
        asyncFuture.set(future);
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> asyncCall(Callable<T> callable) {
        try {
            async.set(Boolean.TRUE);
            final T result = callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            async.remove();
        }
        return new FutureAdapter(asyncFuture.get());
    }
}
