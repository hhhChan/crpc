package com.can.rpc.rpc.protocol;

import com.can.rpc.rpc.*;
import com.can.rpc.rpc.context.TraceContext;
import com.can.rpc.rpc.trace.CrpcTrace;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

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
        Transaction transaction = Cat.newTransaction("CRPC-Comsummer", invocation.getServiceName().concat(".").concat(invocation.getMethodName()));
        trace();
        invocation.setInvokeMode(findInvokeMode(invocation));
        invocation.setTrace(TraceContext.get());
        AsyncResult result;

        try {
            result = (AsyncResult) doInvoke(invocation);
            transaction.setStatus(Transaction.SUCCESS);
            if (InvokeMode.SYNC == invocation.getInvokeMode() && !invocation.getAsync()) {
                result.get(60, TimeUnit.SECONDS);
            }
            Cat.logMetricForCount("CRPC-COMSUMMER-".concat(invocation.getServiceName()));
        } catch (Throwable e) {
            CompletableFuture<SyncResult> future = new CompletableFuture<>();
            SyncResult syncResult = new SyncResult();
            syncResult.setException(e);
            transaction.setStatus(e.getCause());
            Cat.logError(e);
            Cat.logMetricForCount("CRPC-COMSUMMER-FAIL-".concat(invocation.getServiceName()));
            return new AsyncResult(future, invocation);
        } finally {
            transaction.complete();
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

    public void trace() {
        MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
        String messageId = tree.getMessageId();

        if (messageId == null) {
            messageId = Cat.createMessageId();
            tree.setMessageId(messageId);
        }

        String childId = Cat.getProducer().createRpcServerId("default");

        String root = tree.getRootMessageId();

        if (root == null) {
            root = messageId;
        }
        Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

        CrpcTrace crpcTrace = new CrpcTrace();
        crpcTrace.setChildId(childId);
        crpcTrace.setParentId(messageId);
        crpcTrace.setRootId(root);
        TraceContext.set(crpcTrace);
    }
}
