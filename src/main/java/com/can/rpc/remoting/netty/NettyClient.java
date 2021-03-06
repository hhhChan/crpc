package com.can.rpc.remoting.netty;

import com.can.rpc.common.tools.Constans;
import com.can.rpc.remoting.Client;
import com.can.rpc.remoting.Codec;
import com.can.rpc.remoting.CrpcChannel;
import com.can.rpc.remoting.Handler;
import com.can.rpc.rpc.protocol.cprotocol.codec.CrpcDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author ccc
 */
public class NettyClient implements Client {

    private Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final Integer RECONNECT_SECONDS = 20;

    private CrpcChannel channel;

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private URI uri;

    private String serverHost;

    private Integer serverPort;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

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
                            ch.pipeline().addLast(new CrpcDecoder(codec.createIstance()))
                                    .addLast(new NettyCodec(codec.createIstance()))
                                    .addLast(new IdleStateHandler(Constans.DEFAULT_HEARTBEAT, 0, 0))
                                    .addLast(new NettyClientHandler(handler, NettyClient.this));
                        }
                    });
            //????????????
            this.uri = uri;
            this.setServerHost(uri.getHost());
            this.setServerPort(uri.getPort());
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        //????????????
        try {
            bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync().
                addListener((ChannelFutureListener) future -> {
                    // ????????????
                    if (!future.isSuccess()) {
                        logger.error("[Crpc Comsumer ???????????????({}:{}) ??????]", serverHost, serverPort);
                        reconnect();
                        return;
                    }
                    // ????????????
                    channel = new NettyChannel(future.channel());
                    logger.info("[Crpc Comsumer ???????????????({}:{}) ??????]", serverHost, serverPort);
                });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @PreDestroy
    public void shutdown() {
        synchronized (NettyClient.class) {
            // ?????? CrpcChannel??? Netty Channel
            if (channel != null) {
                channel.close();
            }
            group.shutdownGracefully();
        }
    }

    public void reconnect() {
        group.schedule(new Runnable() {
            @Override
            public void run() {
                logger.info("[????????????]");
                connect();
            }
        }, RECONNECT_SECONDS, TimeUnit.SECONDS);
        logger.info("[{} ?????????????????????]", RECONNECT_SECONDS);
    }

    @Override
    public CrpcChannel getChannel() {
        return channel;
    }
}
