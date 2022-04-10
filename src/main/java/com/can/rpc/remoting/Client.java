package com.can.rpc.remoting;

import java.net.URI;

/**
 * @author ccc
 */
public interface Client {

    void open(URI uri, Codec codec, Handler handler);

    CrpcChannel getChannel();

    void connect();
}
