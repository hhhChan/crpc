package com.can.rpc.rpc.proxy;

import com.can.rpc.rpc.context.AsyncContext;
import com.can.rpc.rpc.Invoker;
import com.can.rpc.rpc.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ccc
 */
public class InvokerInvocationHandler implements InvocationHandler {

    private final Invoker invoker;

    public InvokerInvocationHandler(Invoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, objects);
        }

        String methodName = method.getName();
        Class<?>[] paramterTypes = method.getParameterTypes();
        if (paramterTypes.length == 0) {
            //直接本地调用
            if ("toString".equals(methodName)) {
                return invoker.toString();
            } else if ("$destroy".equals(methodName)) {
                return null;
            } else if ("hashCode".equals(methodName)) {
                return invoker.hashCode();
            }
        } else if (paramterTypes.length == 1 && "equals".equals(methodName)) {
            return invoker.equals(objects[0]);
        }

        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setMethodName(methodName);
        rpcInvocation.setAgruments(objects);
        //记得设置这个 不然会导致后面反射找不到方法
        rpcInvocation.setParameterTypes(paramterTypes);
        rpcInvocation.setServiceName(method.getDeclaringClass().getName());
        rpcInvocation.setReturnType(method.getReturnType());
        rpcInvocation.setAsync(AsyncContext.async.get());
        AsyncContext.async.set(Boolean.FALSE);
        return invoker.invoke(rpcInvocation).recreate();
    }
}
