package com.zextras.consul;

import com.zextras.consul.config.ClientConfig;
import com.zextras.consul.monitoring.ClientEventCallback;

abstract class BaseCacheableClient extends BaseClient {

    private final Consul.NetworkTimeoutConfig networkTimeoutConfig;

    protected BaseCacheableClient(String name, ClientConfig config, ClientEventCallback eventCallback,
                                  Consul.NetworkTimeoutConfig networkTimeoutConfig) {
        super(name, config, eventCallback);
        this.networkTimeoutConfig = networkTimeoutConfig;
    }

    public Consul.NetworkTimeoutConfig getNetworkTimeoutConfig() {
        return networkTimeoutConfig;
    }
}
