package com.zextras.consul.cache;

import com.google.common.primitives.Ints;
import com.zextras.consul.HealthClient;
import com.zextras.consul.config.CacheConfig;
import com.zextras.consul.model.health.HealthCheck;
import com.zextras.consul.option.QueryOptions;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class HealthCheckCache extends ConsulCache<String, HealthCheck> {

    private HealthCheckCache(HealthClient healthClient,
                             com.zextras.consul.model.State state,
                             int watchSeconds,
                             QueryOptions queryOptions,
                             Function<HealthCheck, String> keyExtractor,
                             Scheduler callbackScheduler) {
        super(keyExtractor,
            (index, callback) -> {
                checkWatch(healthClient.getNetworkTimeoutConfig().getClientReadTimeoutMillis(), watchSeconds);
                QueryOptions params = watchParams(index, watchSeconds, queryOptions);
                healthClient.getChecksByState(state, params, callback);
            },
            healthClient.getConfig().getCacheConfig(),
            healthClient.getEventHandler(),
            new CacheDescriptor("health.state", state.getName()),
            callbackScheduler);
    }

    /**
     * Factory method to construct a string/{@link HealthCheck} map for a particular {@link com.zextras.consul.model.State}.
     * <p/>
     * Keys will be the {@link HealthCheck#getCheckId()}.
     *
     * @param healthClient the {@link HealthClient}
     * @param state        the state fo filter checks
     * @return a cache object
     */
    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.zextras.consul.model.State state,
            final int watchSeconds,
            final QueryOptions queryOptions,
            final Function<HealthCheck, String> keyExtractor,
            final ScheduledExecutorService callbackExecutorService) {

        Scheduler callbackScheduler = createExternal(callbackExecutorService);
        return new HealthCheckCache(healthClient, state, watchSeconds, queryOptions, keyExtractor, callbackScheduler);
    }

    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.zextras.consul.model.State state,
            final int watchSeconds,
            final QueryOptions queryOptions,
            final Function<HealthCheck, String> keyExtractor) {

        return new HealthCheckCache(healthClient, state, watchSeconds, queryOptions, keyExtractor, createDefault());
    }
    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.zextras.consul.model.State state,
            final int watchSeconds,
            final QueryOptions queryOptions) {

        return newCache(healthClient, state, watchSeconds, queryOptions, HealthCheck::getCheckId);
    }

    public static HealthCheckCache newCache(
            final HealthClient healthClient,
            final com.zextras.consul.model.State state,
            final int watchSeconds) {

        return newCache(healthClient, state, watchSeconds, QueryOptions.BLANK);
    }

    public static HealthCheckCache newCache(final HealthClient healthClient, final com.zextras.consul.model.State state) {
        CacheConfig cacheConfig = healthClient.getConfig().getCacheConfig();
        int watchSeconds = Ints.checkedCast(cacheConfig.getWatchDuration().getSeconds());
        return newCache(healthClient, state, watchSeconds);
    }

}
