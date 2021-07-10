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
import sun.awt.geom.AreaOp;


import java.net.URI;

public class NettyClient implements Client {
    CrpcChannel channel = null;
    EventLoopGroup group = null;

    @Override
    public void connecct(URI uri, Codec codec, Handler handler) {
        try {
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    //指定nio传输channel
                    .channel(NioSocketChannel.class)
                    //添加handler
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyCodec(codec.createInstance()));
                            ch.pipeline().addLast(new NettyHandler(handler));
                        }
                    });
            //同步连接
            ChannelFuture future = bootstrap.connect(uri.getHost(), uri.getPort()).sync();
            channel = new NettyChannel(future.channel());

            //优雅停机
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("我要停机了");
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

    public void setChannel(CrpcChannel channel) {
        this.channel = channel;
    }
}
