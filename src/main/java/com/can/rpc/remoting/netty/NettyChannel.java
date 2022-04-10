package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.CrpcChannel;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author ccc
 */
public class NettyChannel implements CrpcChannel {

    private static final Map<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(Object message) {
        this.setAttribute(NettyClientHandler.KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        this.channel.writeAndFlush(message);
    }

    public static NettyChannel getOrAddChannel(Channel channel) {
        if (channel == null) {
            return null;
        }
        NettyChannel ch =  CHANNEL_MAP.get(channel);
        if (ch == null) {
            NettyChannel nettyChannel = new NettyChannel(channel);
            if (channel.isActive()) {
                ch = CHANNEL_MAP.putIfAbsent(channel, nettyChannel);
            }
            if (ch == null) {
                ch = nettyChannel;
            }
        }
        return ch;
    }

    @Override
    public void close() {
        removeChannelIfNoActive(channel);
        channel.close();
    }

    @Override
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public static void removeChannelIfNoActive(Channel ch) {
        if (ch != null && !ch.isActive()) {
            CHANNEL_MAP.remove(ch);
        }
    }
}
