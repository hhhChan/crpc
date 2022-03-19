package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.Codec;
import com.can.rpc.remoting.Handler;
import com.can.rpc.remoting.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author ccc
 */
public class NettyServer implements Server {

    private EventLoopGroup boss = new NioEventLoopGroup(1);
    private EventLoopGroup workers = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32));

    @Override
    public void start(URI uri, Codec codec, Handler handler) {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, workers)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(uri.getHost(), uri.getPort()))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyCodec(codec.createIstance()));
                            ch.pipeline().addLast(new NettyHandler(handler));
                        }
                    });
            //记得 bind() 绑定
            ChannelFuture future = bootstrap.bind().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
