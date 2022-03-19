package com.can.rpc.common.serialize;

/**
 * @author ccc
 */
public interface Serialization {

    byte[] serialize(Object output) throws Exception;

    Object deserialize(byte[] input, Class c) throws Exception;
}
