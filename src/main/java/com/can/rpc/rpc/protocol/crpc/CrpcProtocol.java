package com.can.rpc.rpc.protocol.crpc;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.tools.SpiUtil;
import com.can.rpc.common.tools.URIUtil;
import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Transporter;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.protocol.Protocol;
import com.can.rpc.rpc.protocol.crpc.codec.CrpcCodec;
import com.can.rpc.rpc.protocol.crpc.handler.CrpcClientHandler;
import com.can.rpc.rpc.protocol.crpc.handler.CrpcServerHandler;

import java.net.URI;

public class CrpcProtocol implements Protocol {
    @Override
    public void export(URI exportUri, Invoker invoker) {
        // 找到序列化
        String serializationName = URIUtil.getParam(exportUri, "serialization");
        Serialization serialization = (Serialization) SpiUtil.getServiceImpl(serializationName, Serialization.class);
        // 1. 编解码器
        CrpcCodec trpcCodec = new CrpcCodec();
        trpcCodec.setDecodeType(RpcInvocation.class);
        trpcCodec.setSerialization(serialization);
        // 2. 收到请求处理器
        CrpcServerHandler crpcServerHandler = new CrpcServerHandler();
        crpcServerHandler.setInvoker(invoker);
        crpcServerHandler.setSerialization(serialization);
        // 3. 底层网络框架
        String transporterName = URIUtil.getParam(exportUri, "transporter");
        Transporter transporter = (Transporter) SpiUtil.getServiceImpl(transporterName, Transporter.class);
        // 4. 启动服务
        transporter.start(exportUri, trpcCodec, crpcServerHandler);

    }

    @Override
    public Invoker refer(URI consumerUri) {
        // 找到序列化
        String serializationName = URIUtil.getParam(consumerUri, "serialization");
        Serialization serialization = (Serialization) SpiUtil.getServiceImpl(serializationName, Serialization.class);
        // 1. 编解码器
        CrpcCodec trpcCodec = new CrpcCodec();
        trpcCodec.setDecodeType(Response.class);
        trpcCodec.setSerialization(serialization);
        // 2. 收到请求处理器
        CrpcClientHandler crpcClientHandler = new CrpcClientHandler();
        // 3. 底层网络框架 连接 -- 长连接
        String transporterName = URIUtil.getParam(consumerUri, "transporter");
        Transporter transporter = (Transporter) SpiUtil.getServiceImpl(transporterName, Transporter.class);
        Client connect = transporter.connect(consumerUri, trpcCodec, crpcClientHandler);
        // 4. 创建一个invoker 通过网络连接发送数据
        CrpcClientInvoker crpcClientInvoker = new CrpcClientInvoker(connect, serialization);
        return crpcClientInvoker;
    }
}
