package com.can.rpc.config.spring;

import com.can.rpc.config.ProtocolConfig;
import com.can.rpc.config.RegistryConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Field;

//将自己创建的对象 放到 spring beandefinition
public class CRPCConfiguration implements ImportBeanDefinitionRegistrar {
    StandardEnvironment environment;

    public CRPCConfiguration(Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }

    //让spring启动的时候，装置 没有注解的 xml配置
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        BeanDefinitionBuilder beanDefinitionBuilder = null;

        //读取配置 复制 crpc.protocol.*
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProtocolConfig.class);
        for (Field field : ProtocolConfig.class.getDeclaredFields()) {
            String value = environment.getProperty("crpc.protocol." + field.getName());
            beanDefinitionBuilder.addPropertyValue(field.getName(), value);
        }
        registry.registerBeanDefinition("protocolConfig", beanDefinitionBuilder.getBeanDefinition());

        //读取配置 复制 crpc.registry.*
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RegistryConfig.class);
        for (Field field : ProtocolConfig.class.getDeclaredFields()) {
            String value = environment.getProperty("crpc.registry." + field.getName());
            beanDefinitionBuilder.addPropertyValue(field.getName(), value);
        }
        registry.registerBeanDefinition("registryConfig", beanDefinitionBuilder.getBeanDefinition());
    }
}
