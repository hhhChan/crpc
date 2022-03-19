package com.can.rpc.rpc;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ccc
 */
public interface Result extends Serializable {

    /**
     * 获取result信息
     */
    Object getValue();

    Result get() throws ExecutionException, InterruptedException;

    Result get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException;

    void set(Object value);

    /**
     * 设置异常信息
     */
    boolean hasException();

    void setException(Throwable e);

    Throwable getException();

    // 根据状态返回 result
    Object recreate() throws Throwable;
}
