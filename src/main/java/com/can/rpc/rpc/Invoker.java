package com.can.rpc.rpc;

/**
 * @author ccc
 */
public interface Invoker {

    //返回接口
    Class getInterface();

    // 发起调用 注意负载均衡 重连等等
    Result invoke(RpcInvocation invocation) throws Exception;
}
