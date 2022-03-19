package com.can.rpc.remoting;

import java.net.URI;

/**
 * @author ccc
 */
public interface Transporter {

    Server start(URI uri, Codec codec, Handler handler);

    Client connect(URI uri, Codec codec, Handler handler);
}
