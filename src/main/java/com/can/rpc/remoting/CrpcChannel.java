package com.can.rpc.remoting;

public interface CrpcChannel {
    //无需管协议 代表一个客户端连接， 因为不同的底层网络框架，这个连接的定义也是不同的，所以做一个接口
    void send(byte[] msg);
}
