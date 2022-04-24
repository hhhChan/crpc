package com.can.rpc.rpc.cluster;

import com.can.rpc.common.serviceloader.CrpcServiceDirectory;
import com.can.rpc.common.tools.SpiUtil;
import com.can.rpc.config.ReferenceConfig;
import com.can.rpc.config.RegistryConfig;
import com.can.rpc.registry.NotifyListener;
import com.can.rpc.registry.RegistryService;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.Result;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.context.CrpcContext;
import com.can.rpc.rpc.protocol.Protocol;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class ClusterInvoker implements Invoker {

    private ReferenceConfig referenceConfig;
    //代表这个服务能够调用的所有实例
    private Map<URI, Invoker> invokerMap = new ConcurrentHashMap<>();
    private LoadBalance loadBalance;
    private static Invoker stickInvoker;

    public ClusterInvoker(ReferenceConfig referenceConfig) throws Exception {
        this.referenceConfig = referenceConfig;
        loadBalance = CrpcServiceDirectory
                .getServiceLoader(LoadBalance.class).getService(referenceConfig.getLoadbalance());
                //(LoadBalance) SpiUtil.getServiceImpl(referenceConfig.getLoadbalance(), LoadBalance.class);
        //接口类全名
        String serviceName = referenceConfig.getService().getName();
        List<RegistryConfig> registryConfigs = referenceConfig.getRegistryConfigs();
        for (RegistryConfig registryConfig : registryConfigs) {
            URI uri = new URI(registryConfig.getAddress());
            RegistryService registryService = CrpcServiceDirectory
                    .getServiceLoader(RegistryService.class).getService(uri.getScheme());
            registryService.init(uri);
            registryService.subscribe(serviceName, new NotifyListener() {
                @Override
                public void notify(Set<URI> uris) {
                    for (URI uri : invokerMap.keySet()) {
                        if (!uris.contains(uri)) {
                            invokerMap.remove(uri);
                        }
                    }

                    //新增一个invoker
                    for (URI uri : uris) {
                        if (!invokerMap.containsKey(uri)) {
                            Protocol protocol = CrpcServiceDirectory
                                    .getServiceLoader(Protocol.class).getService(uri.getScheme());
                            Invoker invoker = protocol.refer(uri);
                            invokerMap.putIfAbsent(uri, invoker);
                        }
                    }
                }
            });
        }
    }

    @Override
    public Class getInterface() {
        return referenceConfig.getService();
    }

    @Override
    public Result invoke(RpcInvocation invocation) throws Exception {
        if (CrpcContext.isStick()) {
            if (stickInvoker != null) {
                return stickInvoker.invoke(invocation);
            } else {
                stickInvoker = loadBalance.select(invokerMap);
                return stickInvoker.invoke(invocation);
            }
        }
        Invoker invoker = loadBalance.select(invokerMap);
        return invoker.invoke(invocation);
    }
}
