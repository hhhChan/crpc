package com.can.rpc.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ccc
 */
public class ServiceConfig {

    private List<RegistryConfig> registryConfigs;

    private List<ProtocolConfig> protocolConfigs;

    private Class service;

    private Object reference;

    private String version;

    public synchronized void addProrocolConfig(ProtocolConfig protocolConfig) {
        if (protocolConfigs == null) {
            protocolConfigs = new ArrayList<>();
        }
        protocolConfigs.add(protocolConfig);
    }

    public synchronized void addRegistryConfig(RegistryConfig registryConfig) {
        if (registryConfigs == null) {
            registryConfigs = new ArrayList<>();
        }
        registryConfigs.add(registryConfig);
    }

    public List<ProtocolConfig> getProtocolConfigs() {
        return protocolConfigs;
    }

    public void setProtocolConfigs(List<ProtocolConfig> protocolConfigs) {
        this.protocolConfigs = protocolConfigs;
    }

    public Class getService() {
        return service;
    }

    public void setService(Class service) {
        this.service = service;
    }

    public Object getReference() {
        return reference;
    }

    public void setReference(Object reference) {
        this.reference = reference;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<RegistryConfig> getRegistryConfigs() {
        return registryConfigs;
    }

    public void setRegistryConfigs(List<RegistryConfig> registryConfigs) {
        this.registryConfigs = registryConfigs;
    }
}
