package com.can.rpc.common.tools;

import java.util.ServiceLoader;

/**
 * @author ccc
 */
public class SpiUtil {

    public static Object getServiceImpl(String name, Class classType) {
        ServiceLoader serviceLoader = ServiceLoader.load(classType, Thread.currentThread().getContextClassLoader());
        for (Object s : serviceLoader) {
            if (s.getClass().getSimpleName().equals(name)) {
                return s;
            }
        }
        return null;
    }
}
