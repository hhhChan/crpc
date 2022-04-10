package com.can.rpc.remoting.netty;

import com.can.rpc.common.tools.Constans;
import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Codec;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class NettyClient implements Client {

    private CrpcChannel channel;

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private URI uri;

    @Override
    public void open(URI uri, Codec codec, Handler handler) {
        try {
            group = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32));
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyCodec(codec.createIstance()))
                                    .addLast(new IdleStateHandler(Constans.DEFAULT_HEARTBEAT, 0, 0))
                                    .addLast(new NettyClientHandler(handler, NettyClient.this));
                        }
                    });
            //同步连接
            this.uri = uri;
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        //同步连接
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync();
            channel = new NettyChannel(future.channel());

            //优雅停机 -- kill pid -- 响应退出信号
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (NettyServer.class) {
                            group.shutdownGracefully().sync();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public CrpcChannel getChannel() {
        return channel;
    }
}
