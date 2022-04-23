package com.can.rpc.rpc.protocol.cprotocol.handler;

import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.remoting.netty.NettyChannel;
import com.can.rpc.rpc.Response;
import com.can.rpc.rpc.SyncResult;
import com.can.rpc.rpc.context.TraceContext;
import com.can.rpc.rpc.trace.CrpcTrace;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class CrpcClientHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(CrpcClientHandler.class);

    final static Map<Long, CompletableFuture<Object>> invokerMap = new ConcurrentHashMap<>();

    public static CompletableFuture<Object> waitResult(long messageId) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        invokerMap.put(messageId, future);
        return future;
    }

    @Override
    public void onReceive(CrpcChannel cprotocol, Object msg) throws Exception {
        Response response = (Response) msg;
        trace(response.getTrace());
        Transaction transaction = Cat.newTransaction("CRPC-Comsummer", "async-over");
        CompletableFuture<Object> future = invokerMap.get(response.getRequsetId());
        if (response.getStatus() == Response.SUCCESS) {
            future.complete(response.getContent());
            transaction.complete();
        } else {
            future.completeExceptionally(new Exception(response.getErrInfo()));
        }
        invokerMap.remove(response.getRequsetId());
    }

    @Override
    public void onWrite(CrpcChannel cprotocol, Object msg) throws Exception {

    }

    public void trace(CrpcTrace crpcTrace) {
        if (crpcTrace.getRootId() != null) {
            String rootId = crpcTrace.getRootId ();
            String childId = crpcTrace.getChildId ();
            String parentId = crpcTrace.getParentId ();
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            tree.setParentMessageId(parentId);
            tree.setRootMessageId(rootId);
            tree.setMessageId(childId);
            CrpcTrace currentTrace = new CrpcTrace ();
            currentTrace.setParentId(childId);
            currentTrace.setRootId(rootId);
        } else {
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            String messageId = tree.getMessageId();
            if (messageId == null) {
                messageId = Cat.createMessageId();
                tree.setMessageId(messageId);
            }
            String root = tree.getRootMessageId();

            if (root == null) {
                root = messageId;
            }
            CrpcTrace currentTrace = new CrpcTrace();
            currentTrace.setParentId(messageId);
            currentTrace.setRootId(root);
        }
    }
}
