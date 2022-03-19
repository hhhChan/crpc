package com.can.rpc.rpc.cluster;

import com.can.rpc.rpc.Invoker;

import java.net.URI;
import java.util.Map;

/**
 * @author ccc
 */
public interface LoadBalance {

    public Invoker select(Map<URI, Invoker> invokerMap);
}
