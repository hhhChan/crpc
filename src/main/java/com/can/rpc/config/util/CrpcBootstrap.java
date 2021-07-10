package com.can.rpc.config.util;

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

public class CrpcBootstrap {

    //暴露service服务
    public static void export(ServiceConfig serviceConfig) {
        // 1. 代理对象
        Invoker invoker = ProxyFactory.getInvoker(serviceConfig.getReference(), serviceConfig.getService());
        try {
            // invoker对象
            // 2根据服务定义的协议，依次暴露。 如果有多个协议那就暴露多次
            for (ProtocolConfig protocolConfig : serviceConfig.getProtocolConfigs()) {
                // 2.1 组织URL --- 协议://ip:端口/service全类名?配置项=值&配置型2=值...
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(protocolConfig.getName() + "://");
                // 此处可选择具体网卡设备 -
                String hostAddress = NetworkInterface.getNetworkInterfaces().
                        nextElement().getInterfaceAddresses().get(0).getAddress().getHostAddress();
                stringBuilder.append(hostAddress + ":");
                stringBuilder.append(protocolConfig.getPort() + "/");
                stringBuilder.append(serviceConfig.getService().getName() + "?");
                // ....版本号啥的的不写了，意思一下吧
                stringBuilder.append("transporter=" + protocolConfig.getTransporter());
                stringBuilder.append("&serialization=" + protocolConfig.getSerialization());

                URI exportUri = new URI(stringBuilder.toString());
                System.out.println("准备暴露服务：" + exportUri);

                // 2.2 创建服务 -- 多个service 用同一个端口 TODO 思考点：一个系统，多个service需要暴露
                Protocol protocol = (Protocol) SpiUtil.getServiceImpl(protocolConfig.getName(), Protocol.class);
                protocol.export(exportUri, invoker);

                // 注册到中心
                for (RegistryConfig registryConfig : serviceConfig.getRegistryConfigs()) {
                    URI registryUri = new URI(registryConfig.getAddress());
                    RegistryService registryService =
                            (RegistryService) SpiUtil.getServiceImpl(registryUri.getScheme(), RegistryService.class);
                    registryService.init(registryUri);
                    registryService.register(exportUri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个代理，用于注入
     */
    public static Object getReferenceBean(ReferenceConfig referenceConfig) {
        try {
            // 根据服务 通过注册中心，找到服务提供者实例
            ClusterInvoker clusterInvoker = new ClusterInvoker(referenceConfig);
            // 代理对象
            Object proxy = ProxyFactory.getProxy(clusterInvoker, new Class[]{referenceConfig.getService()});
            return proxy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}