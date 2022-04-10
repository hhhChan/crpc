package com.can.rpc.rpc.protocol.cprotocol.handler;

import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.remoting.netty.NettyChannel;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.SyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class CrpcClientHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(CrpcClientHandler.class);

    final static Map<Long, CompletableFuture<Object>> invokerMap = new ConcurrentHashMap<>();

    public static CompletableFuture<Object> waitResult(long messageId) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        invokerMap.put(messageId, future);
        return future;
    }

    @Override
    public void onReceive(CrpcChannel cprotocol, Object msg) throws Exception {
        Response response = (Response) msg;
        CompletableFuture<Object> future = invokerMap.get(response.getRequsetId());
        if (response.getStatus() == Response.SUCCESS) {
            if (response.getHeartbeat()) {
                return;
            }
            future.complete(response.getContent());
        } else {
            future.completeExceptionally(new Exception(response.getErrInfo()));
        }
        invokerMap.remove(response.getRequsetId());
    }

    @Override
    public void onWrite(CrpcChannel cprotocol, Object msg) throws Exception {

    }
}
