package com.can.rpc.rpc.protocol.cprotocol.handler;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.*;

import java.util.concurrent.*;

/**
 * @author ccc
 */
public class CrpcServerHandler implements Handler {

    //todo 后续考虑是否spi化 commsumer端是否添加线程池
    private static final ExecutorService sharedExecutorService = new ThreadPoolExecutor(200, 200, 0, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());

    //msg -> RpcInvocation
    @Override
    public void onReceive(CrpcChannel cprotocol, Object msg) throws Exception {
        //todo 目前实现是全部转发给线程池，后续考虑是否添加一个handler来做分发详情
        sharedExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                RpcInvocation invocation = (RpcInvocation) msg;

                Response response = new Response();

                if (invocation.getHeartbeat()) {
                    response.setStatus(Response.SUCCESS);
                    response.setHeartbeat(Boolean.TRUE);
                    cprotocol.send(response);
                    return;
                }
                try {
                    SyncResult result = (SyncResult) getInvoker().invoke(invocation);
                    response.setRequsetId(invocation.getId());
                    response.setStatus(Response.SUCCESS);
                    response.setContent(result);
                    if (CompletableFuture.class.isAssignableFrom(invocation.getReturnType())) {
                        result.set(((CompletableFuture)result.getValue()).get());
                        response.setContent(result);
                    }
                    //byte[] responseBody = getSerialization().serialize(response);
                    cprotocol.send(response);
                } catch (Exception e) {
                    response.setStatus(Response.ERROR);
                    response.setErrInfo(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onWrite(CrpcChannel cprotocol, Object msg) throws Exception {

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
