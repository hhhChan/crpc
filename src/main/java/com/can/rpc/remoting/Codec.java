package com.can.rpc.remoting;

import java.util.List;

/**
 * 注意线程安全
 * @author ccc
 */
public interface Codec {

    byte[] encode(Object msg) throws Exception;

    List<Object> decode(byte[] msg) throws Exception;

    Codec createIstance();
}
