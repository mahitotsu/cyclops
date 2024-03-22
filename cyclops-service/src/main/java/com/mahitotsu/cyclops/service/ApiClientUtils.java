package com.mahitotsu.cyclops.service;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiClientUtils {

    public static <T> T createHttpServiceProxy( final Class<T> httpInterface, final String hostname, final int port) {

        if (httpInterface == null || hostname == null) {
            throw new IllegalArgumentException("The httpInterface and hostname are required.");
        }

        final RestClient restClient = RestClient.builder().baseUrl("http://" + hostname + ":" + port).build();
        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(httpInterface);
    }
}
