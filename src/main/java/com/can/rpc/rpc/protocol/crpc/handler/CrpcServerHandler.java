package com.can.rpc.rpc.protocol.crpc.handler;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.RpcInvocation;

public class CrpcServerHandler implements Handler {
    //msg就是rpcinvocation
    @Override
    public void onReceive(CrpcChannel crpcChannel, Object msg) throws Exception {
        RpcInvocation rpcInvocation = (RpcInvocation) msg;
        System.out.println("收到rpcInvocation信息：" + rpcInvocation);
        // 发出数据 -- response
        Response response = new Response();
        try {
            // 调用目标 接口实现类
            Object result = getInvoker().invoke(rpcInvocation);
            response.setRequestId(rpcInvocation.getId());
            response.setStatus(200);
            response.setContent(result);
            System.out.println("服务端执行结果：" + result);
        } catch (Throwable e) {
            response.setStatus(99);
            response.setContent(e.getMessage());
            e.printStackTrace();
        }
        // 发送数据
        byte[] responseBody = getSerialization().serialize(response);
        crpcChannel.send(responseBody); // write方法
    }

    @Override
    public void onWrite(CrpcChannel crpcChannel, Object msg) throws Exception {

    }

    Invoker invoker;

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Invoker getInvoker() {
        return this.invoker;
    }

    Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Serialization getSerialization() {
        return this.serialization;
    }
}
