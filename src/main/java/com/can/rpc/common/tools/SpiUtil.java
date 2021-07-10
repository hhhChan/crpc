package com.can.rpc.common.tools;

import java.util.ServiceLoader;

public class SpiUtil {
    public  static  Object getServiceImpl(String serviceName, Class classType) {
        ServiceLoader services = ServiceLoader.load(classType, Thread.currentThread().getContextClassLoader());

        for(Object s : services) {
            if (s.getClass().getSimpleName().equals(serviceName)) {
                return s;
            }
        }
        return null;
    }
}
