package com.can.rpc.rpc.cluster.loadbalance;

import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.cluster.LoadBalance;

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ccc
 */
public class RamdomLoadBalance implements LoadBalance {

    @Override
    public Invoker select(Map<URI, Invoker> invokerMap) throws Exception {
        if (invokerMap.values().size() == 0) {
            throw new Exception("not found service");
        }
        int index = ThreadLocalRandom.current().nextInt(invokerMap.values().size());
        return invokerMap.values().toArray(new Invoker[]{})[index];
    }
}
