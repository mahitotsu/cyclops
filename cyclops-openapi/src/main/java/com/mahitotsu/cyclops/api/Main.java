package com.mahitotsu.cyclops.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.mahitotsu.cyclops.api.tickets.TicketsApi;

@SpringBootApplication
public class Main {

    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).build(args).run(args);
    }

    @RestController
    public static class TicketsApiController implements TicketsApi {
    }

    @EventListener
    @SuppressWarnings("null")
    public void afterWebServerInitialized(final WebServerInitializedEvent event) {

        // get webserver info
        final WebServer webServer = event.getWebServer();
        final int port = webServer.getPort();

        // build rest client
        final RestOperations client = new RestTemplate();

        // get openapi document
        final String apidocs = client.getForObject(String.format("http://localhost:%d/v3/api-docs", port),
                String.class);
        System.err.println(apidocs);

        // complete
        webServer.destroy();
    }
}