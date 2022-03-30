package com.can.rpc.rpc.protocol;

import com.can.rpc.rpc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ccc
 */
public abstract class AbstactInvoker implements Invoker {
    @Override
    public Class getInterface() {
        return null;
    }

    @Override
    public Result invoke(RpcInvocation invocation) {
        invocation.setInvokeMode(findInvokeMode(invocation));
        AsyncResult result;

        try {
            result = (AsyncResult) doInvoke(invocation);
        } catch (Throwable e) {
            CompletableFuture<SyncResult> future = new CompletableFuture<>();
            SyncResult syncResult = new SyncResult();
            syncResult.setException(e);
            return new AsyncResult(future, invocation);
        }

        if (InvokeMode.SYNC == invocation.getInvokeMode() && !invocation.getAsync()) {
            try {
                result.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private InvokeMode findInvokeMode(RpcInvocation invocation) {
        if (invocation.getInvokeMode() != null) {
            return invocation.getInvokeMode();
        } else if (isReturnTypeFuture(invocation)) {
            return InvokeMode.ASYNC;
        }
        return InvokeMode.SYNC;
    }

    private boolean isReturnTypeFuture(RpcInvocation invocation) {
       Class<?> clazz = invocation.getReturnType();
       return clazz != null && CompletableFuture.class.isAssignableFrom(clazz);
    }

    protected abstract Result doInvoke(RpcInvocation invocation) throws Throwable;
}
