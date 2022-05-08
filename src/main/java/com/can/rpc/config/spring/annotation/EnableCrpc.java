package com.can.rpc.config.spring.annotation;

import com.can.rpc.config.spring.CrpcConfiguration;
import com.can.rpc.config.spring.CrpcPostprocessor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ccc
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CrpcPostprocessor.class, CrpcConfiguration.class})
@PropertySource("classpath:/crpc.properties")
@ComponentScan()
public @interface EnableCrpc {
}
