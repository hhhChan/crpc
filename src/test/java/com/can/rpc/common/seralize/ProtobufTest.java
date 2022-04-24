package com.can.rpc.common.seralize;

import com.can.rpc.common.serialize.json.JsonSerialization;
import com.can.rpc.common.serialize.protobuf.RpcInvocationProto;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.RpcInvocation;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

/**
 * @author ccc
 */
public class ProtobufTest {

    @Test
    public void test() {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setId(100L);
        invocation.setMethodName("");
        RpcInvocationProto.RpcInvocation.Builder rpcBuil = RpcInvocationProto.RpcInvocation.newBuilder();
        rpcBuil.setId(invocation.getId());
        rpcBuil.setServiceName(invocation.getServiceName());
        rpcBuil.setMethodName(invocation.getMethodName());
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        for (int i = 0; i < invocation.getParameterTypes().length; i++) {
            rpcBuil.setTypes(i, parameterTypes[i].getName());
        }
        JsonSerialization serialization = new JsonSerialization();

        Object[] agruments = invocation.getAgruments();
        for (int i = 0; i < agruments.length; i++) {
            try {
                ByteString bytes = ByteString.copyFrom(serialization.serialize(agruments[i]));
                rpcBuil.setAgrs(i, bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        rpcBuil.setAsync(invocation.getAsync());
        rpcBuil.setHeartbeat(invocation.getHeartbeat());
        RpcInvocationProto.RpcInvocation.Trace.Builder trace = RpcInvocationProto.RpcInvocation.Trace.newBuilder();
        trace.setParentId(invocation.getTrace().getParentId());
        trace.setChildId(invocation.getTrace().getChildId());
        trace.setRootId(invocation.getTrace().getRootId());
        rpcBuil.setTrace(trace);

        RpcInvocationProto.RpcInvocation rpcInvocation = rpcBuil.build();
        byte[] haha = rpcInvocation.toByteArray();
        try {
            rpcInvocation = RpcInvocationProto.RpcInvocation.parseFrom(haha);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        RpcInvocation res = new RpcInvocation();

    }

    @Test
    public void testClass() {
        Class c = Response.class;
        System.out.println(c == Response.class);

    }
}
