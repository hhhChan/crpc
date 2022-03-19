package com.can.rpc.remoting;

import java.net.URI;

/**
 * @author ccc
 */
public interface Client {

    void connect(URI uri, Codec codec, Handler handler);

    CrpcChannel getChannel();
}
