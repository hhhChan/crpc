package com.can.rpc.rpc.protocol.cprotocol;

import com.can.rpc.rpc.SyncResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author ccc
 */
public class FutureAdapter<V> extends CompletableFuture<V> {

    private CompletableFuture<SyncResult> future;

    public FutureAdapter(CompletableFuture<SyncResult> future) {
        this.future = future;
        this.future.whenComplete((syncResult, t) -> {
            if (t != null) {
                this.completeExceptionally(t);
            } else {
                if (syncResult.hasException()) {
                    this.completeExceptionally(syncResult.getException());
                } else {
                    this.complete((V) syncResult.getValue());
                }
            }
        });
    }
}
