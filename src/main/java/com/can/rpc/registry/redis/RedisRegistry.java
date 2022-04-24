package com.can.rpc.registry.redis;

import com.can.rpc.common.tools.URIUtil;
import com.can.rpc.registry.NotifyListener;
import com.can.rpc.registry.RegistryService;
import org.apache.log4j.pattern.LevelPatternConverter;
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

/**
 * @author ccc
 */
public class RedisRegistry implements RegistryService {

    private static final int TIME_OUT = 15;
    private URI address;
    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    //服务提供者
    private ArrayList<URI> serviceHeartBeat = new ArrayList<>();
    //服务消费者
    private JedisPubSub jedisPubSub;
    private Map<String, Set<URI>> localCache = new ConcurrentHashMap<>();
    private Map<String, NotifyListener> listenerMap = new ConcurrentHashMap<>();

    @Override
    public void registry(URI uri) {
        String key = "crpc-" + uri.toString();
        Jedis jedis = new Jedis(address.getHost(), address.getPort());
        jedis.setex(key, TIME_OUT, String.valueOf(System.currentTimeMillis()));
        jedis.close();
        //开始心跳
        serviceHeartBeat.add(uri);
    }

    @Override
    public void unregistry(URI uri) {
        String key = "crpc-" + uri.toString();
        Jedis jedis = new Jedis(address.getHost(), address.getPort());
        jedis.del(key);
        jedis.close();
        //移除心跳
        serviceHeartBeat.remove(uri);
    }

    @Override
    public void subscribe(String name, NotifyListener listener) {
        try {
            if (localCache.get(name) == null) {
                localCache.putIfAbsent(name, new HashSet<>());
                listenerMap.putIfAbsent(name, listener);
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                Set<String> services = jedis.keys("crpc-*" + name + "?*");
                for (String service : services) {
                    URI uri = new URI(service.replace("crpc-", ""));
                    localCache.get(name).add(uri);
                }
                listener.notify(localCache.get(name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(String name, NotifyListener listener) {

    }

    @Override
    public void init(URI address) {
        this.address = address;
        //启动定时任务
        executorService.scheduleAtFixedRate((Runnable) () -> {
            try {
                Jedis jedis = new Jedis(address.getHost(), address.getPort());
                for (URI service : serviceHeartBeat) {
                    jedis.expire("crpc-" + service.toString(), TIME_OUT);
                }
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3000, 5000, TimeUnit.MILLISECONDS);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    jedisPubSub = new JedisPubSub() {
                        @Override
                        public void onPMessage(String pattern, String channel, String message) {
                            try {
                                URI uri = new URI(channel.replace("__keyspace@0__:crpc-",""));
                                if ("set".equals(message)) {
                                    Set<URI> uris = localCache.get(URIUtil.getService(uri));
                                    if (uris != null) {
                                        uris.add(uri);
                                    }
                                }
                                if ("expired".equals(message)) {
                                    Set<URI> uris = localCache.get(URIUtil.getService(uri));
                                    if (uris != null) {
                                        uris.remove(uri);
                                    }
                                }
                                if ("set".equals(message) || "expired".equals(message)) {
                                    NotifyListener listener = listenerMap.get(URIUtil.getService(uri));
                                    if (listener != null) {
                                        listener.notify(localCache.get(URIUtil.getService(uri)));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onPSubscribe(String channel, int subscribedChannels) {
                            //todo 后续实现注册到注册中心
                        }
                    };
                    Jedis jedis = new Jedis(address.getHost(), address.getPort());
                    jedis.psubscribe(jedisPubSub, "__keyspace@0__:crpc-");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
