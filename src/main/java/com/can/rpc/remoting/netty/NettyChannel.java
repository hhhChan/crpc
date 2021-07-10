package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.CrpcChannel;
import io.netty.channel.Channel;

public class NettyChannel implements CrpcChannel {
    Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(byte[] msg) {
        channel.writeAndFlush(msg);
    }
}
