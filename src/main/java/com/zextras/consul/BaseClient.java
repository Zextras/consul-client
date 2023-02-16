package com.zextras.consul;

import com.zextras.consul.config.ClientConfig;
import com.zextras.consul.monitoring.ClientEventCallback;
import com.zextras.consul.monitoring.ClientEventHandler;
import com.zextras.consul.util.Http;

abstract class BaseClient {

    private final ClientConfig config;
    private final ClientEventHandler eventHandler;
    protected final Http http;

    protected BaseClient(String name, ClientConfig config, ClientEventCallback eventCallback) {
        this.config = config;
        this.eventHandler = new ClientEventHandler(name, eventCallback);
        this.http = new Http(eventHandler);
    }

    public ClientConfig getConfig() {
        return config;
    }

    public ClientEventHandler getEventHandler() { return eventHandler; }
}
