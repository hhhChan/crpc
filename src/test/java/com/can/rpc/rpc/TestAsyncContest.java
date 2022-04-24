package com.can.rpc.rpc;

import com.can.rpc.rpc.context.CrpcContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author ccc
 */
public class TestAsyncContest {

    @Test
    public void testThreadLocal() throws IOException {
        CrpcContext.async.set(Boolean.FALSE);
        new Thread(() -> {
            CrpcContext.async.set(Boolean.TRUE);
            System.out.println("2-" + CrpcContext.async.get());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("2-" + CrpcContext.async.get());
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("1-" + CrpcContext.async.get());
    }
}
