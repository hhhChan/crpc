package com.can.rpc.remoting;

import java.net.URI;

public interface Client {
    void connecct(URI uri, Codec codec, Handler handler);

    CrpcChannel getChannel();
}
