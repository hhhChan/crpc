package com.can.rpc.remoting;

import java.net.URI;

public interface Server {
    void start(URI uri, Codec coedc, Handler handler);
}
