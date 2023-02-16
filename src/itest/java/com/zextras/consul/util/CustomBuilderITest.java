package com.zextras.consul.util;

import com.zextras.consul.BaseIntegrationTest;
import com.zextras.consul.Consul;
import com.zextras.consul.model.agent.Agent;
import org.junit.Test;

import java.net.Proxy;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;

public class CustomBuilderITest extends BaseIntegrationTest{

    @Test
    public void shouldConnectWithCustomTimeouts() throws UnknownHostException {
        Consul client = Consul.builder()
                .withHostAndPort(defaultClientHostAndPort)
                .withProxy(Proxy.NO_PROXY)
                .withConnectTimeoutMillis(10000)
                .withReadTimeoutMillis(3600000)
                .withWriteTimeoutMillis(900)
                .build();
        Agent agent = client.agentClient().getAgent();
        assertNotNull(agent);
    }

}
