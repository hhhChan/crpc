package com.can.rpc.registry.redis;

import com.can.rpc.common.tools.URIUtil;
import com.can.rpc.registry.NotifyListener;
import com.can.rpc.registry.RegistryService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisRegistry implements RegistryService {
    private static final int TIME_OUT = 15; //15秒过期
    URI address;
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);

    //服务提供者相关
    ArrayList<URI> serviceHeartBeat = new ArrayList<>();

    //服务消费者相关
    JedisPubSub jedisPubSub;
    Map<String, Set<URI>> localCache = new ConcurrentHashMap<>();
    Map<String, NotifyListener> listenerMap = new ConcurrentHashMap<>();

    @Override
    public void register(URI uri) {
        String key = "crpc-" + uri.toString();
        Jedis jedis = new Jedis(address.getHost(), address.getPort());
        jedis.setex(key, TIME_OUT, String.valueOf(System.currentTimeMillis()));
        jedis.close();
        //开始心跳
        serviceHeartBeat.add(uri);
    }

    @Override
    public void subscribe(String service, NotifyListener notifyListener) {
        try {
            if (localCache.get(service) == null) {
                localCache.putIfAbsent(service, new HashSet<>());
                listenerMap.putIfAbsent(service, notifyListener);
                // 第一次直接获取
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                String key = "crpc-" + service;
                Set<String> serviceInstances = jedis.keys("crpc-*" + service + "?*");
                for (String instances : serviceInstances) {
                    URI instanceUri = new URI(instances.replace("crpc-", ""));
                    localCache.get(service).add(instanceUri);
                }
                notifyListener.notify(localCache.get(service));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(URI address) {
        this.address = address;

        // 启动定时任务
        executorService.scheduleWithFixedDelay((Runnable) () -> {
            try {
                // -- 心跳 -- 延长时间
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                for (URI service : serviceHeartBeat) {
                    String key = "crpc-" + service.toString();
                    jedis.expire(key, TIME_OUT);
                }
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 3000, 5000, TimeUnit.MILLISECONDS);

        // 监听服务变动 - redis.conf  notify-keyspace-events KE$xg
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    jedisPubSub = new JedisPubSub() {
                        @Override
                        public void onPSubscribe(String pattern, int subscribedChannels) {
                            System.out.println("注册中心开始监听:" + pattern);
                        }

                        @Override
                        public void onPMessage(String pattern, String channel, String message) {
                            try {
                                URI serviceURI = new URI(channel.replace("__keyspace@0__:crpc-", ""));
                                if ("set".equals(message)) {
                                    // 新增
                                    Set<URI> uris = localCache.get(URIUtil.getService(serviceURI));
                                    if (uris != null) {
                                        uris.add(serviceURI);
                                    }
                                }
                                if ("expired".equals(message)) {
                                    // 过期
                                    Set<URI> uris = localCache.get(URIUtil.getService(serviceURI));
                                    if (uris != null) {
                                        uris.remove(serviceURI);
                                    }
                                }
                                if ("set".equals(message) || "expired".equals(message)) {
                                    System.out.println("服务实例有变化，开始刷新");
                                    NotifyListener notifyListener = listenerMap.get(URIUtil.getService(serviceURI));
                                    if (notifyListener != null) {
                                        notifyListener.notify(localCache.get(URIUtil.getService(serviceURI)));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Jedis jedis = new Jedis(address.getHost(), address.getPort());
                    jedis.psubscribe(jedisPubSub, "__keyspace@0__:crpc-*");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}