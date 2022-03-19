package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Codec;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author ccc
 */
public class NettyClient implements Client {

    private CrpcChannel channel;

    private EventLoopGroup group;

    @Override
    public void connect(URI uri, Codec codec, Handler handler) {
        try {
            group = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32));
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyCodec(codec.createIstance()));
                            ch.pipeline().addLast(new NettyHandler(handler));
                        }
                    });
            //同步连接
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public CrpcChannel getChannel() {
        return channel;
    }
}
