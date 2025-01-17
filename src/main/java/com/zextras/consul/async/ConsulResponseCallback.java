package com.zextras.consul.async;

import com.zextras.consul.model.ConsulResponse;

/**
 * For API calls that support long-polling, this callback is used to handle
 * the result on success or failure for an async HTTP call.
 *
 * @param <T> The Response type.
 */
public interface ConsulResponseCallback<T> {

    /**
     * Callback for a successful {@link com.zextras.consul.model.ConsulResponse}.
     *
     * @param consulResponse The Consul response.
     */
    void onComplete(ConsulResponse<T> consulResponse);

    /**
     * Callback for an unsuccessful request.
     *
     * @param throwable The exception thrown.
     */
    void onFailure(Throwable throwable);
}
