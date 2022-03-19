package com.can.rpc.common.serviceloader;

import com.can.rpc.common.tools.Holder;
import jdk.nashorn.internal.ir.ReturnNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ccc
 */
public class CrpcServiceLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/services/";
    private static final String CRPC_DIRECTORY = "META-INF/crpc/";
    private final Class<?> type;
    private final Map<String, Holder<Object>> cacheServices = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cacheClasses = new Holder<>();
    private String cachedDefaultName;

    CrpcServiceLoader(Class<?> type) {
        this.type = type;
    }

    public T getService(String name) {
        final Holder<Object> holder = getOrCreateHolder(name);
        Object service = holder.get();
        if (service == null) {
            synchronized (holder) {
                service = holder.get();
                if (service == null) {
                    service = createService(name);
                    holder.set(service);
                }
            }
        }
        if (service == null) {
            return getDefaultExtension();
        }
        return (T) service;
    }

    public T getDefaultExtension() {
        getServiceClasses();
        if ("".equals(cachedDefaultName)) {
            return null;
        }
        return getService(cachedDefaultName);
    }

    private Holder<Object> getOrCreateHolder(String name) {
        if (cacheServices.get(name) == null) {
            cacheServices.putIfAbsent(name, new Holder<>());
        }
        return cacheServices.get(name);
    }

    @SuppressWarnings("unchecked")
    private T createService(String name) {
        Class<?> tClass = getServiceClass(name);
        T instance = null;
        try {
            instance = createInstance((Class<T>)tClass);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return instance;
    }

    private T createInstance(Class<T> tClass) throws ReflectiveOperationException {
        Constructor<T> constructor = null;
        try {
            constructor = tClass.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return constructor.newInstance();
    }

    private Class<?> getServiceClass(String name) {
        return getServiceClasses().get(name);
    }

    private Map<String, Class<?>> getServiceClasses() {
        if (cacheClasses.get() == null) {
            synchronized (cacheClasses) {
                if (cacheClasses.get() == null) {
                    cacheClasses.set(loadServiceClasses());
                }
            }
        }
        return cacheClasses.get();
    }

    private Map<String, Class<?>> loadServiceClasses() {
        Map<String, Class<?>> classes = new HashMap<>();
        String fileName = CRPC_DIRECTORY + type.getSimpleName();
        cacheDefaultServiceName();
        try {
            Enumeration<java.net.URL> resources = ClassLoader.getSystemResources(fileName);
            while (resources.hasMoreElements()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(resources.nextElement().openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        final int skip = line.indexOf('#');
                        if (skip > 0) {
                            line = line.substring(0, skip).trim();
                        }
                        if (line.length() != 0) {
                            String key = null, value;
                            final int i = line.indexOf('=');
                            if (i > 0) {
                                key = line.substring(0, i).trim();
                                value = line.substring(i + 1).trim();
                            } else {
                                value = line.trim();
                            }
                            loadClass(classes, key, Class.forName(value, true, Thread.currentThread().getContextClassLoader()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private void cacheDefaultServiceName() {
        final SPI spi = type.getAnnotation(SPI.class);
        if (spi == null) {
            return;
        }
        cachedDefaultName = spi.value();
    }

    private void loadClass(Map<String, Class<?>> classes, String name, Class<?> clazz) {
        if (name == null) {
            return;
        }
        String[] names = name.split(",");
        Arrays.stream(names).forEach(className -> classes.put(className, clazz));
    }
}
