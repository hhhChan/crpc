package com.can.rpc.rpc;
/*
* 1消费者调用服务，通过Invoker对象
* 2服务提供者调用，具体实现类，也通过Invoker对象
* */

public interface Invoker {
    /**
     *
     * @return 返回接口
     */
    Class getInterface();

    //发起调用【负载均衡、容错、重连..都在这里面了】
    Object invoke(RpcInvocation rpcInvocation) throws Exception;
}
