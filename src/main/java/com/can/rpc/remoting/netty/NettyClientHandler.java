package com.can.rpc.remoting.netty;

import com.can.rpc.common.tools.Constans;
import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ccc
 */
public class NettyClientHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private Handler handler;

    private Client client;

    public static final String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";

    public static final String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";

    public NettyClientHandler(Handler handler, Client client) {
        this.handler = handler;
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        channel.setAttribute(KEY_READ_TIMESTAMP, now());
        handler.onReceive(channel, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // send heartbeat when read idle.
        if (evt instanceof IdleStateEvent) {
            try {
                logger.info("[userEventTriggered][发起一次心跳]");
                sendHeartbeat(ctx.channel());
            } finally {
                NettyChannel.removeChannelIfNoActive(ctx.channel());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartbeat(Channel ch) {

        NettyChannel nettyChannel = NettyChannel.getOrAddChannel(ch);
        if (!needReconnect(nettyChannel)) {
            RpcInvocation invocation = new RpcInvocation();
            invocation.setHeartbeat(Boolean.TRUE);
            nettyChannel.send(invocation);
        }

    }

    private boolean needReconnect(NettyChannel ch) {
        Long lastRead = (Long) ch.getAttribute(KEY_READ_TIMESTAMP);
        Long now = now();
        if (lastRead != null && (now - lastRead) / 1000 > Constans.HEARTBEAT_TIMEOUT) {
            logger.warn("try to reconneted------------");
            ch.close();
            NettyChannel.removeChannelIfNoActive(ch.getChannel());
            client.connect();
            return true;
        }
        return false;
    }

    private Long now() {
        return System.currentTimeMillis();
    }
}
