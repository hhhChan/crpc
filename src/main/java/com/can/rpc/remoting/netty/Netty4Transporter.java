package com.can.rpc.remoting.netty;

import com.can.rpc.remoting.*;

import java.net.URI;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ccc
 */
public class Netty4Transporter implements Transporter {
    @Override
    public Server start(URI uri, Codec codec, Handler handler) {
        NettyServer server = new NettyServer();
        server.start(uri, codec, handler);
        return server;
    }

    @Override
    public Client connect(URI uri, Codec codec, Handler handler) {
        NettyClient client = new NettyClient();
        client.open(uri, codec, handler);
        return client;
    }
}
