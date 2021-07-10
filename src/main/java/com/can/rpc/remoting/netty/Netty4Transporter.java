package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.*;

import java.net.URI;

public class Netty4Transporter implements Transporter {
    @Override
    public Server start(URI uri, Codec codec, Handler handler) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(uri, codec, handler);
        return nettyServer;
    }

    @Override
    public Client connect(URI uri, Codec codec, Handler handler) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connecct(uri, codec, handler);
        return nettyClient;
    }
}
