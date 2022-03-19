package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Handler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author ccc
 */
public class NettyHandler extends ChannelDuplexHandler {

    private Handler handler;

    public NettyHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handler.onReceive(new NettyChannel(ctx.channel()), msg);
    }
}
