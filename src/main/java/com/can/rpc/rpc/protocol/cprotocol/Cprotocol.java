package com.can.rpc.rpc.protocol.cprotocol;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.serviceloader.CrpcServiceDirectory;
import com.can.rpc.common.tools.SpiUtil;
import com.can.rpc.common.tools.URIUtil;
import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Transporter;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.protocol.cprotocol.codec.CrpcCodec;
import com.can.rpc.rpc.protocol.cprotocol.handler.CrpcClientHandler;
import com.can.rpc.rpc.protocol.cprotocol.handler.CrpcServerHandler;
import com.can.rpc.rpc.protocol.Protocol;

import java.net.URI;

/**
 * @author ccc
 */
public class Cprotocol implements Protocol {

    @Override
    public void export(URI uri, Invoker invoker) {
        //todo 后续考虑为null情况
        String serializationName = URIUtil.getParamter(uri, "serialization", "JsonSerialization");
        Serialization serialization = CrpcServiceDirectory
                .getServiceLoader(Serialization.class).getService(serializationName);

        //编码
        CrpcCodec codec = new CrpcCodec();
        codec.setSerialization(serialization);
        codec.setDecodeType(RpcInvocation.class);
        //收到请求处理器
        CrpcServerHandler handler = new CrpcServerHandler();
        handler.setInvoker(invoker);
        handler.setSerialization(serialization);
        //获得底层网络架构
        String transporterName = URIUtil.getParamter(uri, "transporter", "Netty4Transporter");
        Transporter transporter = CrpcServiceDirectory
                .getServiceLoader(Transporter.class).getService(transporterName);
        transporter.start(uri, codec, handler);
    }

    @Override
    public Invoker refer(URI uri) {
        String serializationName = URIUtil.getParamter(uri, "serialization", "JsonSerialization");
        Serialization serialization = CrpcServiceDirectory
                .getServiceLoader(Serialization.class).getService(serializationName);

        //编码
        CrpcCodec codec = new CrpcCodec();
        codec.setSerialization(serialization);
        codec.setDecodeType(Response.class);

        CrpcClientHandler handler = new CrpcClientHandler();

        //连接
        String transporterName = URIUtil.getParamter(uri, "transporter", "Netty4Transporter");
        Transporter transporter = CrpcServiceDirectory
                .getServiceLoader(Transporter.class).getService(transporterName);
        Client client = transporter.connect(uri, codec, handler);

        CrpcClientInvoker invoker = new CrpcClientInvoker(client, serialization);
        return invoker;
    }
}
