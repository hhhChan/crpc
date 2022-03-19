package com.can.rpc.registry.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author ccc
 */
public class TestZookeeper {
    private static CuratorFramework curatorClient;

    private static String zookssperAddress;

    //@BeforeAll
    public static void setUp() throws Exception {
        zookssperAddress = "localhost:2181";
        curatorClient = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        curatorClient.start();
    }

    @Test
    public void testCreatePath(){
        try {
            curatorClient.create().forPath("a/cca");
        } catch (Exception e) {
            e.printStackTrace();
        }
        curatorClient.close();
    }

    @Test
    public void testString(){
        String a = "abcd";
        if (a.substring(0, 0) == null) {
            System.out.println("hah");
        }
        System.out.println(a.substring(0, 0));
    }
}
