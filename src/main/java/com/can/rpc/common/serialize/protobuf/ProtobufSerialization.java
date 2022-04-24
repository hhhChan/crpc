package com.can.rpc.common.serialize.protobuf;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.serialize.json.JsonSerialization;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.SyncResult;
import com.can.rpc.rpc.trace.CrpcTrace;
import com.google.protobuf.ByteString;

/**
 * @author ccc
 */
public class ProtobufSerialization implements Serialization {

    private final JsonSerialization serialization = new JsonSerialization();;

    @Override
    public byte[] serialize(Object output) throws Exception {
        if (output.getClass() == RpcInvocation.class) {
            RpcInvocation invocation = (RpcInvocation) output;
            RpcInvocationProto.RpcInvocation.Builder rpcBuil = RpcInvocationProto.RpcInvocation.newBuilder();
            rpcBuil.setId(invocation.getId());
            rpcBuil.setServiceName(invocation.getServiceName());
            rpcBuil.setMethodName(invocation.getMethodName());
            Class<?>[] parameterTypes = invocation.getParameterTypes();
            if (parameterTypes !=null && parameterTypes.length != 0) {
                for (int i = 0; i < invocation.getParameterTypes().length; i++) {
                    rpcBuil.addTypes(parameterTypes[i].getName());
                }
            }

            Object[] agruments = invocation.getAgruments();
            if (agruments != null && agruments.length != 0) {
                for (int i = 0; i < agruments.length; i++) {
                    try {
                        ByteString bytes = ByteString.copyFrom(serialization.serialize(agruments[i]));
                        rpcBuil.addAgrs(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            rpcBuil.setAsync(invocation.getAsync());
            rpcBuil.setHeartbeat(invocation.getHeartbeat());
            RpcInvocationProto.RpcInvocation.Trace.Builder trace = RpcInvocationProto.RpcInvocation.Trace.newBuilder();
            trace.setParentId(invocation.getTrace().getParentId());
            trace.setChildId(invocation.getTrace().getChildId());
            trace.setRootId(invocation.getTrace().getRootId());
            rpcBuil.setTrace(trace);

            return rpcBuil.build().toByteArray();
        } else {
            Response response = (Response) output;
            ResponseProto.Response.Builder responseBuild = ResponseProto.Response.newBuilder();
            responseBuild.setRequsetId(response.getRequsetId());
            responseBuild.setHeartbeat(response.getHeartbeat());
            if (response.getErrInfo() != null) {
                responseBuild.setErrinfo(response.getErrInfo());
            }
            responseBuild.setStatus(ByteString.copyFrom(new byte[]{response.getStatus()}));
            ResponseProto.Response.Trace.Builder trace = ResponseProto.Response.Trace.newBuilder();
            if (response.getTrace().getChildId() != null) {
                trace.setChildId(response.getTrace().getChildId());
            }
            trace.setParentId(response.getTrace().getParentId());
            trace.setRootId(response.getTrace().getRootId());
            responseBuild.setTrace(trace);
            ResponseProto.Response.SyncResult.Builder result = ResponseProto.Response.SyncResult.newBuilder();
            result.setContext(ByteString.copyFrom(serialization.serialize(response.getContent().getValue())));
            if (response.getContent().getException() != null) {
                result.setException(response.getContent().getException().getMessage());
            }
            responseBuild.setResult(result);
            return responseBuild.build().toByteArray();
        }

    }

    @Override
    public Object deserialize(byte[] input, Class c) throws Exception {
        if (c == RpcInvocation.class) {
            RpcInvocationProto.RpcInvocation rpcInvocation = RpcInvocationProto.RpcInvocation.parseFrom(input);
            RpcInvocation invocation = new RpcInvocation();
            invocation.setId(rpcInvocation.getId());
            invocation.setServiceName(rpcInvocation.getServiceName());
            invocation.setMethodName(rpcInvocation.getMethodName());

            int lenth = rpcInvocation.getTypesCount();
            Class<?>[] parameterTypes = new Class[lenth];
            for (int i = 0; i < lenth; i++) {
                parameterTypes[i] = Class.forName(rpcInvocation.getTypes(i));
            }
            invocation.setParameterTypes(parameterTypes);
            lenth = rpcInvocation.getAgrsCount();
            Object[] agruments = new Object[lenth];
            for (int i = 0; i < lenth; i++) {
                try {
                    agruments[i] = serialization.deserialize(rpcInvocation.getAgrs(i).toByteArray(), parameterTypes[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            invocation.setAgruments(agruments);
            invocation.setAsync(rpcInvocation.getAsync());
            invocation.setHeartbeat(rpcInvocation.getHeartbeat());
            CrpcTrace trace = new CrpcTrace();
            trace.setRootId(rpcInvocation.getTrace().getRootId());
            trace.setParentId(rpcInvocation.getTrace().getParentId());
            trace.setChildId(rpcInvocation.getTrace().getChildId());
            invocation.setTrace(trace);
            return invocation;
        } else {
            ResponseProto.Response responseproto = ResponseProto.Response.parseFrom(input);
            Response response = new Response();
            response.setRequsetId(responseproto.getRequsetId());
            response.setStatus(responseproto.getStatus().byteAt(0));
            if (responseproto.getErrinfo() != null && !responseproto.getErrinfo().isEmpty()) {
                response.setErrInfo(responseproto.getErrinfo());
            }
            response.setHeartbeat(responseproto.getHeartbeat());
            CrpcTrace crpcTrace = new CrpcTrace();
            crpcTrace.setRootId(responseproto.getTrace().getRootId());
            crpcTrace.setChildId(responseproto.getTrace().getChildId());
            crpcTrace.setParentId(responseproto.getTrace().getParentId());
            response.setTrace(crpcTrace);
            SyncResult result = new SyncResult();
            result.setResult(serialization.deserialize(responseproto.getResult().getContext().toByteArray(), Object.class));
            if (responseproto.getResult().getException() != null && !responseproto.getResult().getException().isEmpty()) {
                result.setException(new Throwable(responseproto.getResult().getException()));
            }
            response.setContent(result);
            return response;
        }
    }
}
