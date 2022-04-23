package com.can.rpc.common.serialize.protobuf;

import com.can.rpc.common.serialize.Serialization;

/**
 * @author ccc
 */
public class ProtobufSerialization implements Serialization {
    @Override
    public byte[] serialize(Object output) throws Exception {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] input, Class c) throws Exception {
        return null;
    }
}
