package com.can.rpc.remoting;

/**
 * @author ccc
 */
public interface Handler {

    //收到数据 发过来的请求-服务器给的响应
    void onReceive(CrpcChannel cprotocol, Object msg) throws Exception;

    void onWrite(CrpcChannel cprotocol, Object msg) throws Exception;
}
