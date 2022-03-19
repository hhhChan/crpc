package com.can.rpc.registry;

import java.net.URI;
import java.util.Set;

/**
 * @author ccc
 */
public interface NotifyListener {

    public void notify(Set<URI> uris);
}
