package com.can.rpc.rpc;

import com.can.rpc.rpc.protocol.cprotocol.FutureAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ccc
 */
public class AsyncResult implements Result{

    private CompletableFuture<SyncResult> syncFuture;
    private RpcInvocation invocation;

    public AsyncResult(CompletableFuture<SyncResult> future, RpcInvocation invocation) {
        this.syncFuture = future;
        this.invocation = invocation;
    }

    @Override
    public Object getValue() {
        return getSyncResult().getValue();
    }

    @Override
    public Result get() throws ExecutionException, InterruptedException {
        return syncFuture.get();
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return syncFuture.get(timeout, unit);
    }

    @Override
    public void set(Object value) {
        try {
            if (syncFuture.isDone()) {
                syncFuture.get().set(value);
            } else {
                SyncResult result = new SyncResult();
                result.set(value);
                syncFuture.complete(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean hasException() {
        return getSyncResult().hasException();
    }

    @Override
    public void setException(Throwable e) {
        try {
            if (syncFuture.isDone()) {
                syncFuture.get().setException(e);
            } else {
                SyncResult result = new SyncResult();
                result.setException(e);
                syncFuture.complete(result);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public Throwable getException() {
        return getSyncResult().getException();
    }

    @Override
    public Object recreate() throws Throwable {
        if (InvokeMode.ASYNC == invocation.getInvokeMode()) {
            return new FutureAdapter(syncFuture);
        } else if (invocation.getAsync()) {
            AsyncContext.setAsyncFuture(syncFuture);
            return null;
        }
        return getSyncResult().recreate();
    }

    public Result getSyncResult() {
        try {
            if (syncFuture.isDone()) {
                return syncFuture.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildDefaultResult(invocation);
    }

    private Result buildDefaultResult(RpcInvocation invocation) {
        //todo 解决默认返回值
        return new SyncResult();
    }
}
