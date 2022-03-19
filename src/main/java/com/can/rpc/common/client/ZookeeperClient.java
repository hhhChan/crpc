package com.can.rpc.common.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 采用curtor进行简单的操作封装
 * @author ccc
 */
public class ZookeeperClient implements Client{

    protected static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    protected int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    protected int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;

    private final CuratorFramework client;
    private static final Object PRESENT = new Object();
    private final Map<String, Object> existPath = new ConcurrentHashMap<>();

    public ZookeeperClient(URI uri) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(uri.getHost() + ":" + uri.getPort())
                .retryPolicy(new RetryNTimes(1, 1000))
                .connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT_MS)
                .sessionTimeoutMs(DEFAULT_SESSION_TIMEOUT_MS);
        client = builder.build();
        client.start();
    }


    @Override
    public void create(String path, boolean ephemeral) {
        try {
            if (!ephemeral) {
                if (existPath.containsKey(path)) {
                    return;
                } else if (checkEphemeralExist(path)) {
                    existPath.put(path, PRESENT);
                    return;
                }
                checkPrePath(path);
                client.create().forPath(path);
            } else {
                checkPrePath(path);
                client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            logger.warn("node path = " + path + " Exist, now create " + (ephemeral ? "ephemeral" : "persistent") + " node");
            delete(path);
            create(path, ephemeral);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPrePath(String path) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
    }

    private boolean checkEphemeralExist(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void delete(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            //当删除的节点不存在时 可以忽略
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        client.close();
        //todo 清除监听器
    }

    @Override
    public boolean connect() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public List<String> getChild(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
