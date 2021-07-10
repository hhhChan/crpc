package com.can.rpc.rpc.protocol.crpc.handler;

import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CrpcClientHandler implements Handler {
    final static Map<Long, CompletableFuture> invokerMap = new ConcurrentHashMap<>();

    // 登记一下， 创建返回一个future -- 每一个等待结果的线程一个单独的future
    public static CompletableFuture waitResult(long messageId) {
        CompletableFuture future = new CompletableFuture();
        invokerMap.put(messageId, future);
        return future;
    }

    // 客户端而已，收到 响应 --- 方法执行的返回值
    // 这个方法 -- 网络框架的线程
    @Override
    public void onReceive(CrpcChannel crpcChannel, Object msg) throws Exception {
        Response response = (Response) msg;
        // 接收所有的结果 -- 和 invoker调用者不在一个线程
        // 根据id  和 具体 的请求对应起来 complete发送
        invokerMap.get(response.getRequestId()).complete(response);
        invokerMap.remove(response.getRequestId());
    }

    @Override
    public void onWrite(CrpcChannel crpcChannel, Object msg) throws Exception {

    }
}
