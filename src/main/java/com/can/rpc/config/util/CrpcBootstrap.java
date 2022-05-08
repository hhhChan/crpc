package com.can.rpc.config.util;

import com.can.rpc.common.serviceloader.CrpcServiceDirectory;
import com.can.rpc.common.tools.SpiUtil;
import com.can.rpc.config.ProtocolConfig;
import com.can.rpc.config.ReferenceConfig;
import com.can.rpc.config.RegistryConfig;
import com.can.rpc.config.ServiceConfig;
import com.can.rpc.registry.RegistryService;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.cluster.ClusterInvoker;
import com.can.rpc.rpc.protocol.Protocol;
import com.can.rpc.rpc.proxy.ProxyFactory;

import java.net.NetworkInterface;
import java.net.URI;

/**
 * @author ccc
 */
public class CrpcBootstrap {

    public static void export(ServiceConfig serviceConfig) {
        Invoker invoker = ProxyFactory.getInvoker(serviceConfig.getReference(), serviceConfig.getService());

        try {
            for (ProtocolConfig protocolConfig : serviceConfig.getProtocolConfigs()) {
                StringBuilder sb = new StringBuilder();
                sb.append(protocolConfig.getName()).append("://");
                String hostAddress = NetworkInterface.getNetworkInterfaces().nextElement().getInterfaceAddresses()
                        .get(0).getAddress().getHostAddress();
                sb.append(hostAddress)
                        .append(":")
                        .append(protocolConfig.getPort())
                        .append("/")
                        .append(serviceConfig.getService().getName())
                        .append("?")
                        .append("transporter=").append(protocolConfig.getTransporter())
                        .append("&serialization=").append(protocolConfig.getSerialization())
                        .append("&serviceName=").append(serviceConfig.getService().getName())
                        .append("&version=").append(serviceConfig.getVersion());

                URI uri = new URI(sb.toString());
                Protocol protocol = CrpcServiceDirectory.getServiceLoader(Protocol.class).getService(protocolConfig.getName());
                protocol.export(uri, invoker);

                for (RegistryConfig registryConfig : serviceConfig.getRegistryConfigs()) {
                    URI registryUri = new URI(registryConfig.getAddress());
                    RegistryService registryService =  CrpcServiceDirectory.
                            getServiceLoader(RegistryService.class).getService(registryUri.getScheme());
                    registryService.init(registryUri);
                    registryService.registry(uri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //注册代理 用于注入
    public static Object getReferenceBean(ReferenceConfig referenceConfig) {
        try {
            ClusterInvoker invoker = new ClusterInvoker(referenceConfig);
            Object proxy = ProxyFactory.getProxy(invoker, new Class[]{referenceConfig.getService()});
            return proxy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
