package com.can.rpc.rpc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ccc
 */
public class SyncResult implements Result{

    private static final long serialVersionUID = -6925924956850004727L;
    private Object result;
    private Throwable exception;

    public SyncResult() {
    }

    public SyncResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public Object getValue() {
        return result;
    }

    @Override
    public Result get() {
            throw new UnsupportedOperationException("SyncResult don't support get()");
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        throw new UnsupportedOperationException("SyncResult don't support get(long timeout, TimeUnit unit)");
    }

    @Override
    public void set(Object value) {
        this.result = value;
    }

    @Override
    public boolean hasException() {
        return this.exception != null;
    }

    @Override
    public void setException(Throwable e) {
        this.exception = e;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public Object recreate() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }
}
