package com.can.rpc.rpc.context;


import com.can.rpc.rpc.trace.CrpcTrace;

/**
 * @author ccc
 */
public class TraceContext {

    private static ThreadLocal<CrpcTrace> localTrace = new ThreadLocal<>();

    public static CrpcTrace get() {
        return localTrace.get();
    }

    public static void set(CrpcTrace crpcTrace) {
        localTrace.set(crpcTrace);
    }

    public static void remove(){
        localTrace.remove ();
    }
}
