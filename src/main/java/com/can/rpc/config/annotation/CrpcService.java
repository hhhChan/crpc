package com.can.rpc.config.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ccc
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service//可与@componentscan配合
public @interface CrpcService {

    Class<?> interfaceClass() default void.class;

    String version() default "";
}
