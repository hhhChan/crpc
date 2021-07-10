package com.can.rpc.rpc.cluster;

import com.can.rpc.common.tools.SpiUtil;
import com.can.rpc.config.ReferenceConfig;
import com.can.rpc.config.RegistryConfig;
import com.can.rpc.registry.NotifyListener;
import com.can.rpc.registry.RegistryService;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.protocol.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterInvoker implements Invoker {
    ReferenceConfig referenceConfig;

    /**
     * 代表这个服务能够调用的所有实例
     */
    Map<URI, Invoker> invokers = new ConcurrentHashMap<>();

    LoadBalance loadBalance;

    public ClusterInvoker(ReferenceConfig referenceConfig) throws URISyntaxException {
        this.referenceConfig = referenceConfig;
        loadBalance = (LoadBalance) SpiUtil.getServiceImpl(referenceConfig.getLoadbalance(), LoadBalance.class);

        // 接口类的全类名
        String serivceName = referenceConfig.getService().getName();
        // 1. 服务发现 -- 注册中心
        List<RegistryConfig> registryConfigs = referenceConfig.getRegistryConfigs();
        for (RegistryConfig registryConfig : registryConfigs) {
            URI regitryUri = new URI(registryConfig.getAddress());
            RegistryService registryService =
                    (RegistryService) SpiUtil.getServiceImpl(regitryUri.getScheme(), RegistryService.class);
            registryService.init(regitryUri);
            registryService.subscribe(serivceName, new NotifyListener() {
                // 当服务有更新的时候，触发【新增、剔除】
                @Override
                public void notify(Set<URI> uris) {
                    System.out.println("更新前的服务invoker信息" + invokers);
                    // 剔除 - 创建好的invoker，是不是存在于最小的 实例里面
                    for (URI uri : invokers.keySet()) {
                        if (!uris.contains(uri)) {
                            invokers.remove(uri);
                        }
                    }

                    // 新增 - 意味新建一个invoker
                    for (URI uri : uris) {
                        if (!invokers.containsKey(uri)) {
                            // 意味着有一个服务实例
                            Protocol protocol = (Protocol) SpiUtil.getServiceImpl(uri.getScheme(), Protocol.class);
                            Invoker invoker = protocol.refer(uri); // invoker 代表一个长连接
                            // 保存起来
                            invokers.putIfAbsent(uri, invoker);
                        }
                    }
                    System.out.println("更新后的服务invoker信息" + invokers);
                }
            });
        }

    }
    @Override
    public Class getInterface() {
        return referenceConfig.getService();
    }

    @Override
    public Object invoke(RpcInvocation rpcInvocation) throws Exception {
        // invoker 调用一次 -- 这么多invokers调用哪一个。
        Invoker select = loadBalance.select(invokers);
        Object result = select.invoke(rpcInvocation);
        return result;
    }
}
