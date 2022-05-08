package com.can.rpc.rpc.cluster.loadbalance;

import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.cluster.LoadBalance;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ccc
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private static volatile AtomicInteger integer = new AtomicInteger(0);
    @Override
    public Invoker select(Map<URI, Invoker> invokerMap) throws Exception {
        if (invokerMap.values().size() == 0) {
            throw new Exception("not found service");
        }
        int current = integer.getAndIncrement();
        if (current >= Integer.MAX_VALUE) {
            current = 0;
            integer.set(0);
        }
        return invokerMap.values().toArray(new Invoker[]{})[current % invokerMap.size()];
    }
}
