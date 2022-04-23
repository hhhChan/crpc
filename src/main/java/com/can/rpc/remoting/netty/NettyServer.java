package com.can.rpc.remoting.netty;

import com.can.rpc.common.tools.Constans;
import com.can.rpc.remoting.Codec;
import com.can.rpc.remoting.Handler;
import com.can.rpc.remoting.Server;
import com.can.rpc.rpc.protocol.cprotocol.codec.CrpcDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

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
                            ch.pipeline().addLast(new CrpcDecoder(codec.createIstance()))
                                    .addLast(new NettyCodec(codec.createIstance()))
                                    .addLast(new IdleStateHandler(0, 0, Constans.HEARTBEAT_TIMEOUT))
                                    .addLast(new NettyServerHandler(handler));
                        }
                    });
            //记得 bind() 绑定
            bootstrap.bind().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
