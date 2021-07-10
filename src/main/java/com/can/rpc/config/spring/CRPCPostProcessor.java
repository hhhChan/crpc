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

/*
    spring扫描 初始化对象后 查找CrpcService
 */
public class CRPCPostProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //服务提供
        if(bean.getClass().isAnnotationPresent(CrpcService.class)) {
            System.out.println("找到了需要开放网络访问的service实现类，构建serviceConfig配置");
            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.addProtocolConfig(applicationContext.getBean(ProtocolConfig.class));
            serviceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
            serviceConfig.setReference(bean);

            CrpcService crpcService = bean.getClass().getAnnotation(CrpcService.class);
            if(void.class == crpcService.interfaceClass()) {
                serviceConfig.setService(bean.getClass().getInterfaces()[0]);
            } else {
                serviceConfig.setService(crpcService.interfaceClass());
            }

            CrpcBootstrap.export(serviceConfig);
        }

        // 2. 服务引用- 注入
        for (Field field : bean.getClass().getDeclaredFields()) {
            try {
                if (!field.isAnnotationPresent(CrpcReference.class)) {
                    continue; // 不继续下面的代码，继续循环
                }
                // 引用相关 配置 保存在一个对象里边 // TODO 思考：如果一个引用需要在多个类被使用
                ReferenceConfig referenceConfig = new ReferenceConfig();
                referenceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
                referenceConfig.addProtocolConfig(applicationContext.getBean(ProtocolConfig.class));
                referenceConfig.setService(field.getType());

                CrpcReference cRpcReference = field.getAnnotation(CrpcReference.class);
                referenceConfig.setLoadbalance(cRpcReference.loadbalance());

                Object referenceBean = CrpcBootstrap.getReferenceBean(referenceConfig);
                field.setAccessible(true);
                field.set(bean, referenceBean);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }
}
