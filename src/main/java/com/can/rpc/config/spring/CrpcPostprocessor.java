package com.can.rpc.config.spring;

import com.can.rpc.config.ProtocolConfig;
import com.can.rpc.config.ReferenceConfig;
import com.can.rpc.config.RegistryConfig;
import com.can.rpc.config.ServiceConfig;
import com.can.rpc.config.annotation.CrpcReference;
import com.can.rpc.config.annotation.CrpcService;
import com.can.rpc.config.util.CrpcBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;

/**
 * @author ccc
 */
public class CrpcPostprocessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(CrpcService.class)) {
            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.addProrocolConfig(applicationContext.getBean(ProtocolConfig.class));
            serviceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
            serviceConfig.setReference(bean);

            CrpcService crpcService = bean.getClass().getAnnotation(CrpcService.class);
            if (void.class == crpcService.interfaceClass()) {
                serviceConfig.setService(bean.getClass().getInterfaces()[0]);
            } else {
                serviceConfig.setService(crpcService.interfaceClass());
            }
            CrpcBootstrap.export(serviceConfig);
        }


        for (Field field : bean.getClass().getDeclaredFields()) {
            try {
                if (!field.isAnnotationPresent(CrpcReference.class)) {
                    continue;
                }
                ReferenceConfig referenceConfig = new ReferenceConfig();
                referenceConfig.addProrocolConfig(applicationContext.getBean(ProtocolConfig.class));
                referenceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
                referenceConfig.setService(field.getType());
                CrpcReference crpcReference = field.getAnnotation(CrpcReference.class);
                referenceConfig.setLoadbalance(crpcReference.loadbalance());

                Object reference = CrpcBootstrap.getReferenceBean(referenceConfig);
                field.setAccessible(true);
                field.set(bean, reference);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}