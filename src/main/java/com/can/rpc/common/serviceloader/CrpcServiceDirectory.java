package com.can.rpc.common.serviceloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class CrpcServiceDirectory {

    private final static Map<Class<?>, CrpcServiceLoader<?>> cacheServiceLoader = new ConcurrentHashMap<>(64);

    public static <T> CrpcServiceLoader<T> getServiceLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Service type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Service type (" + type + ") is not an interface!");
        }

        CrpcServiceLoader<T> serviceLoader = (CrpcServiceLoader<T>) cacheServiceLoader.get(type);
        if (serviceLoader == null) {
            cacheServiceLoader.putIfAbsent(type, new CrpcServiceLoader<T>(type));
        }
        return (CrpcServiceLoader<T>) cacheServiceLoader.get(type);
    }
}
