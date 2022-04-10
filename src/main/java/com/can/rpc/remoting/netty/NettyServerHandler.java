package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Handler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ccc
 */
public class NettyServerHandler  extends ChannelDuplexHandler{

        private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
        private Handler handler;

        public NettyServerHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            handler.onReceive(new NettyChannel(ctx.channel()), msg);
        }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // server will close channel when server don't receive any heartbeat from client util timeout.
        if (evt instanceof IdleStateEvent) {
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
            try {
                logger.info("IdleStateEvent triggered, close channel " + channel);
                channel.close();
            } finally {
                NettyChannel.removeChannelIfNoActive(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}