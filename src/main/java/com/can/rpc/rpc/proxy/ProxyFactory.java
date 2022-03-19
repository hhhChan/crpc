package com.can.rpc.rpc.proxy;


import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.Result;
import com.can.rpc.rpc.RpcInvocation;
import com.can.rpc.rpc.SyncResult;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ccc
 */
public class ProxyFactory {

    public static Object getProxy(Invoker invoker, Class<?>[] interfaces) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new InvokerInvocationHandler(invoker));
    }

    public static Invoker getInvoker(Object proxy, Class type) {
        return new Invoker() {
            @Override
            public Class getInterface() {
                return type;
            }

            @Override
            public Result invoke(RpcInvocation invocation) throws Exception {
                Method method = proxy.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                return new SyncResult(method.invoke(proxy, invocation.getAgruments()));
            }
        };
    }
}
