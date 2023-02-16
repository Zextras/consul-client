package com.zextras.consul;

import com.zextras.consul.config.ClientConfig;
import com.zextras.consul.monitoring.ClientEventCallback;

/**
 * Allows tests to create KeyValueClient objects.
 */
public class KeyValueClientFactory {
    private KeyValueClientFactory() {
    }

    public static KeyValueClient create(KeyValueClient.Api api, ClientConfig config, ClientEventCallback eventCallback,
                                        Consul.NetworkTimeoutConfig networkTimeoutConfig) {
        return new KeyValueClient(api, config, eventCallback, networkTimeoutConfig);
    }
}
