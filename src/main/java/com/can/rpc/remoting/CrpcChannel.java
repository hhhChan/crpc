package com.can.rpc.remoting;

/**
 * @author ccc
 */
public interface CrpcChannel {

    //不管协议 只要发送数据就行
    void send(Object message);

    void close();

    boolean hasAttribute(String key);

    Object getAttribute(String key);

    void setAttribute(String key, Object value);

    void removeAttribute(String key);
}
