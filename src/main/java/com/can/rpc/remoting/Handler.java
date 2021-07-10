package com.can.rpc.remoting;

//根据协议实现
public interface Handler {
    void onReceive(CrpcChannel crpcChannel, Object msg) throws Exception;

    void onWrite(CrpcChannel crpcChannel, Object msg) throws Exception;
}
