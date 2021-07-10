package com.can.rpc.remoting;

import java.net.URI;

//底层网络传输 - 同一入口[服务、客户端]
public interface Transporter {
    Server start(URI uri, Codec codec, Handler handler);

    Client connect(URI uri, Codec codec, Handler handler);
}
