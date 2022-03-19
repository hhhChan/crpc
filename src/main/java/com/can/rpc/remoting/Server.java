package com.can.rpc.remoting;

import java.net.URI;

/**
 * @author ccc
 */
public interface Server {

    void start(URI uri, Codec codec, Handler handler);
}
