package com.can.rpc.rpc;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ccc
 */
//保留一次调用的信息
public class RpcInvocation implements Serializable {

    private static final long serialVersionUID = -3960018924662656591L;

    private static AtomicLong SEQ = new AtomicLong();

    private long id;

    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] agruments;

    private Boolean async;

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    private transient InvokeMode invokeMode;

    private transient Class<?> returnType;

    public InvokeMode getInvokeMode() {
        return invokeMode;
    }

    public void setInvokeMode(InvokeMode invokeMode) {
        this.invokeMode = invokeMode;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public RpcInvocation() {
        this.setId(incrementAndGet());
    }

    private final long incrementAndGet() {
        long current, next;
        do {
            current = SEQ.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!SEQ.compareAndSet(current, next));

        return next;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static AtomicLong getSEQ() {
        return SEQ;
    }

    public static void setSEQ(AtomicLong SEQ) {
        RpcInvocation.SEQ = SEQ;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getAgruments() {
        return agruments;
    }

    public void setAgruments(Object[] agruments) {
        this.agruments = agruments;
    }

}
