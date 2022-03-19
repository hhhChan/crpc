package com.can.rpc.rpc.protocol;

import com.can.rpc.common.serviceloader.SPI;
import com.can.rpc.rpc.Invoker;

import java.net.URI;

/**
 * @author ccc
 */
@SPI("Cprotocol")
public interface Protocol {

    public void export(URI uri, Invoker invoker);

    public Invoker refer(URI uri);
}
