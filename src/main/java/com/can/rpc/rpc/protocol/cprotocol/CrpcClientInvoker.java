package com.can.rpc.rpc.protocol.cprotocol;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.remoting.Client;
import com.can.rpc.rpc.*;
import com.can.rpc.rpc.protocol.AbstactInvoker;
import com.can.rpc.rpc.protocol.cprotocol.handler.CrpcClientHandler;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author ccc
 */
public class CrpcClientInvoker extends AbstactInvoker {

    private Client client;

    @Override
    public String toString() {
        return "CrpcClientInvoker{" +
                "client=" + client +
                ", serialization=" + serialization +
                '}';
    }

    private Serialization serialization;

    public CrpcClientInvoker(Client client, Serialization serialization) {
        this.client = client;
        this.serialization = serialization;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    @Override
    protected Result doInvoke(RpcInvocation invocation) throws Throwable {
//        byte[] requestBody = serialization.serialize(invocation);
//        this.client.getChannel().send(requestBody);
        //byte[] requestBody = serialization.serialize(invocation);
            this.client.getChannel().send(invocation);
        CompletableFuture<SyncResult> future = CrpcClientHandler.waitResult(invocation.getId())
                    .thenApply(obj -> (SyncResult) obj);
        return new AsyncResult(future, invocation);
    }
}
