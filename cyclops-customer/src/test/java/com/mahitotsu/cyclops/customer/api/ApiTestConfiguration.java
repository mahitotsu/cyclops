package com.mahitotsu.cyclops.customer.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ApiTestConfiguration {

    @Bean
    @Qualifier("client")
    public CustomerApi.Client customerApi() {

        final RestClient restClient = RestClient.builder().baseUrl("http://localhost:" + 8080).build();
        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(CustomerApi.Client.class);
    }
}
