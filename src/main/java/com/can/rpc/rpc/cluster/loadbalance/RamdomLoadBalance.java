package com.can.rpc.rpc.cluster.loadbalance;

import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.cluster.LoadBalance;

import java.net.URI;
import java.util.Map;
import java.util.Random;

/**
 * @author ccc
 */
public class RamdomLoadBalance implements LoadBalance {
    @Override
    public Invoker select(Map<URI, Invoker> invokerMap) {
        if (invokerMap.values().size() == 0) {
            return null;
        }
        int index = new Random().nextInt(invokerMap.values().size());
        return invokerMap.values().toArray(new Invoker[]{})[index];
    }
}
