package com.mahitotsu.cyclops.api;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.service.annotation.HttpExchange;

import com.mahitotsu.cyclops.api.Main.ControllerStub;

import io.github.classgraph.ClassGraph;

@SpringBootApplication
@ComponentScan(excludeFilters = { @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { ControllerStub.class }) })
public class Main {

    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).build(args).run(args);
    }

    @RestController
    public static interface ControllerStub {
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class OpenAPIDefinitionProcessor implements BeanFactoryPostProcessor {

        @SuppressWarnings("null")
        @Override
        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {

            final ClassLoader cl = beanFactory.getBeanClassLoader();
            final ClassGraph classGraph = new ClassGraph().acceptPackages(this.getClass().getPackageName())
                    .enableAnnotationInfo();
            final Set<Class<?>> apiSpecs = classGraph.scan().getClassesWithAnnotation(HttpExchange.class).stream()
                    .map(info -> {
                        try {
                            return ClassUtils.forName(info.getName(), cl);
                        } catch (ClassNotFoundException | LinkageError e) {
                            return null;
                        }
                    }).filter(spec -> spec != null).collect(Collectors.toSet());
            if (apiSpecs.isEmpty()) {
                return;
            }

            final MethodInterceptor handler = new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    final Method method = invocation.getMethod();
                    if (method.getDeclaringClass().isInterface()) {
                        throw new UnsupportedOperationException("Unimplemented method 'invoke'");
                    }
                    return method.invoke(this, invocation.getArguments());
                }
            };

            for (final Class<?> apiSpec : apiSpecs) {
                final ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setInterfaces(ControllerStub.class, apiSpec);
                proxyFactory.addAdvice(handler);
                final Object apiController = proxyFactory.getProxy(beanFactory.getBeanClassLoader());
                beanFactory.registerSingleton(apiController.getClass().getSimpleName(), apiController);
            }
        }
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