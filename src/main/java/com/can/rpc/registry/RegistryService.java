package com.can.rpc.registry;

import com.can.rpc.common.serviceloader.SPI;

import java.net.URI;

/**
 * @author ccc
 */
@SPI("RedisRegistry")
public interface RegistryService {

    void registry(URI uri);

    void unregistry(URI uri);

    void subscribe(String name, NotifyListener listener);

    void unsubscribe(String name, NotifyListener listener);

    void init(URI address);


}
