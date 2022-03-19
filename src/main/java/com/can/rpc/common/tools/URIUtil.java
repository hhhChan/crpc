package com.can.rpc.common.tools;

import java.net.URI;

/**
 * @author ccc
 */
public class URIUtil {

    public static String getParamter(URI uri, String paramName, String defaultValue) {
        for (String param : uri.getQuery().split("&")) {
            if (param.startsWith(paramName + "=")) {
                return param.replace(paramName + "=", "");
            }
        }
        return defaultValue;
    }

    public static String getService(URI uri) {
        return uri.getPath().replace("/", "");
    }
}
