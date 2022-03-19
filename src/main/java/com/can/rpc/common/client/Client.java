package com.can.rpc.common.client;

import java.util.List;

/**
 * @author ccc
 */
public interface Client {

    void create(String path, boolean ephemeral);

    void delete(String path);

    void close();

    boolean connect();

    List<String> getChild(String path);
}
