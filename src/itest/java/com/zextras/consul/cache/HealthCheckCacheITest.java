package com.zextras.consul.cache;

import com.zextras.consul.BaseIntegrationTest;
import com.zextras.consul.HealthClient;
import com.zextras.consul.model.State;
import com.zextras.consul.model.health.HealthCheck;
import com.zextras.consul.Synchroniser;
import org.junit.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HealthCheckCacheITest extends BaseIntegrationTest {

    @Test
    public void cacheShouldContainPassingTestsOnly() throws Exception {
        HealthClient healthClient = client.healthClient();
        String checkName = UUID.randomUUID().toString();
        String checkId = UUID.randomUUID().toString();

        client.agentClient().registerCheck(checkId, checkName, 20L);
        try {
            client.agentClient().passCheck(checkId);
            Synchroniser.pause(Duration.ofMillis(100));

            try (HealthCheckCache hCheck = HealthCheckCache.newCache(healthClient, State.PASS)) {
                hCheck.start();
                hCheck.awaitInitialized(3, TimeUnit.SECONDS);

                HealthCheck check = hCheck.getMap().get(checkId);
                assertEquals(checkId, check.getCheckId());

                client.agentClient().failCheck(checkId);
                Synchroniser.pause(Duration.ofMillis(100));

                check = hCheck.getMap().get(checkId);
                assertNull(check);
            }
        }
        finally {
            client.agentClient().deregisterCheck(checkId);
        }
    }
}
