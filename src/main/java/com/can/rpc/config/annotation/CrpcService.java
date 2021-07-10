package com.can.rpc.config.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface CrpcService {
    /*
     * 若多个接口，自己指定一个
     */
    Class<?>interfaceClass() default void.class;
}
