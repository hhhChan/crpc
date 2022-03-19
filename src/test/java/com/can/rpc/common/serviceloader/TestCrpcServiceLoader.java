package com.can.rpc.common.serviceloader;

import com.can.rpc.common.serialize.Serialization;
import com.can.rpc.common.serialize.json.JsonSerialization;
import com.can.rpc.registry.RegistryService;
import com.can.rpc.remoting.Transporter;
import org.junit.jupiter.api.Test;

/**
 * @author ccc
 */
public class TestCrpcServiceLoader {
    @Test
    public void testGetService(){
        ;
        //System.out.println(CrpcServiceDirectory.getServiceLoader(Serialization.class).getService("JsonSerialization"));
        System.out.println(CrpcServiceDirectory.getServiceLoader(RegistryService.class).getService("RedisRegistry"));

    }
}
