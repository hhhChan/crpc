package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.CrpcChannel;
import io.netty.channel.Channel;


/**
 * @author ccc
 */
public class NettyChannel implements CrpcChannel {

    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(Object message) {
        this.channel.writeAndFlush(message);
    }
}
