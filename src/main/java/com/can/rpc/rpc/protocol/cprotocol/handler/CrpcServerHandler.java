package com.can.rpc.rpc.protocol.cprotocol.handler;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.*;
import com.can.rpc.rpc.context.TraceContext;
import com.can.rpc.rpc.trace.CrpcTrace;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

/**
 * @author ccc
 */
public class CrpcServerHandler implements Handler {

    //todo 后续考虑是否spi化 commsumer端是否添加线程池
    private static final ExecutorService sharedExecutorService = new ThreadPoolExecutor(200, 200, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());

    //msg -> RpcInvocation
    @Override
    public void onReceive(CrpcChannel cprotocol, Object msg) throws Exception {
        //todo 目前实现是全部转发给线程池，后续考虑是否添加一个handler来做分发详情
        sharedExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                RpcInvocation invocation = (RpcInvocation) msg;
                trace(invocation.getTrace());
                Response response = new Response();
                if (invocation.getHeartbeat()) {
                    response.setStatus(Response.SUCCESS);
                    response.setHeartbeat(Boolean.TRUE);
                    cprotocol.send(response);
                    return;
                }
                Transaction transaction = Cat.newTransaction("CRPC-Service", ManagementFactory.getRuntimeMXBean().getName() + " - " + invocation.getServiceName().concat(".").concat(invocation.getMethodName()));
                try {
                    SyncResult result = (SyncResult) getInvoker().invoke(invocation);
                    response.setRequsetId(invocation.getId());
                    response.setStatus(Response.SUCCESS);
                    response.setContent(result);
                    response.setTrace(TraceContext.get());
                    if (invocation.getReturnType() != null && CompletableFuture.class.isAssignableFrom(invocation.getReturnType())) {
                        result.set(((CompletableFuture)result.getValue()).get());
                        response.setContent(result);
                    }
                    //byte[] responseBody = getSerialization().serialize(response);
                    cprotocol.send(response);
                    transaction.setStatus(Transaction.SUCCESS);
                    Cat.logMetricForCount("CRPC-PROVIDER-".concat(invocation.getServiceName()));
                } catch (Exception e) {
                    response.setRequsetId(invocation.getId());
                    response.setStatus(Response.ERROR);
                    response.setErrInfo(e.getMessage());
                    response.setTrace(TraceContext.get());
                    e.printStackTrace();
                    cprotocol.send(response);
                    transaction.setStatus(e.getCause());
                    Cat.logMetricForCount("CRPC-PROVIDER-FAIL-".concat(invocation.getServiceName()));
                } finally {
                    transaction.complete();
                    if (invocation.getTrace().getRootId() != null) {
                        TraceContext.remove();
                    }
                }
            }
        });

    }

    @Override
    public void onWrite(CrpcChannel cprotocol, Object msg) throws Exception {

    }

    public void trace(CrpcTrace crpcTrace) {
        if (crpcTrace.getRootId() != null) {
            String rootId = crpcTrace.getRootId ();
            String childId = crpcTrace.getChildId ();
            String parentId = crpcTrace.getParentId ();
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            tree.setParentMessageId(parentId);
            tree.setRootMessageId(rootId);
            tree.setMessageId(childId);
            CrpcTrace currentTrace = new CrpcTrace ();
            currentTrace.setParentId(childId);
            currentTrace.setRootId(rootId);
            TraceContext.set(currentTrace);
        } else {
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            String messageId = tree.getMessageId();
            if (messageId == null) {
                messageId = Cat.createMessageId();
                tree.setMessageId(messageId);
            }
            String root = tree.getRootMessageId();

            if (root == null) {
                root = messageId;
            }
            CrpcTrace currentTrace = new CrpcTrace();
            currentTrace.setParentId(messageId);
            currentTrace.setRootId(root);
            TraceContext.set(currentTrace);
        }
    }

    private Invoker invoker;

    private Serialization serialization;

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }
}
