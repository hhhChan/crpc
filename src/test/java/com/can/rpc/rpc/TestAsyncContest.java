package com.can.rpc.rpc;

import com.can.rpc.rpc.context.AsyncContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author ccc
 */
public class TestAsyncContest {

    @Test
    public void testThreadLocal() throws IOException {
        AsyncContext.async.set(Boolean.FALSE);
        new Thread(() -> {
            AsyncContext.async.set(Boolean.TRUE);
            System.out.println("2-" + AsyncContext.async.get());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("2-" + AsyncContext.async.get());
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("1-" + AsyncContext.async.get());
    }
}
