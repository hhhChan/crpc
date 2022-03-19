package com.can.rpc.config.spring;

import com.can.rpc.config.ProtocolConfig;
import com.can.rpc.config.ReferenceConfig;
import com.can.rpc.config.RegistryConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Field;

/**
 * @author ccc
 */
public class CrpcConfiguration implements ImportBeanDefinitionRegistrar {

    private StandardEnvironment standardEnvironment;

    public CrpcConfiguration(Environment environment) {
        this.standardEnvironment = (StandardEnvironment) environment;
    }


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProtocolConfig.class);
        for (Field field : ProtocolConfig.class.getDeclaredFields()) {
            //从配置中进行读取赋值
            String value = standardEnvironment.getProperty("crpc.protocol." + field.getName());
            beanDefinitionBuilder.addPropertyValue(field.getName(), value);
        }
        registry.registerBeanDefinition("protocolConfig", beanDefinitionBuilder.getBeanDefinition());

        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RegistryConfig.class);
        for (Field field : RegistryConfig.class.getDeclaredFields()) {
            String value = standardEnvironment.getProperty("crpc.registry." + field.getName());
            beanDefinitionBuilder.addPropertyValue(field.getName(), value);
        }
        registry.registerBeanDefinition("registryConfig", beanDefinitionBuilder.getBeanDefinition());
    }
}
