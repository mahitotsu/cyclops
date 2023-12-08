package com.mahitotsu.cyclops.webapp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new NuxtStaticResolver());
    }

    private static class NuxtStaticResolver implements ResourceResolver {

        @Override
        @Nullable
        public Resource resolveResource(
                final @Nullable HttpServletRequest request,
                final String path,
                final List<? extends Resource> locations,
                final ResourceResolverChain chain) {
            return this.resolveIndexPage(path, locations);
        }

        @Override
        @Nullable
        public String resolveUrlPath(
                final String path,
                final List<? extends Resource> locations,
                final ResourceResolverChain chain) {
            final Resource index = this.resolveIndexPage(path, locations);
            return index != null ? index.getFilename() : null;
        }

        private Resource resolveIndexPage(final String path, final List<? extends Resource> locations) {
            for (final Resource location : locations) {
                try {
                    final Resource resource = location.createRelative(path);
                    if (resource.exists() && resource.isFile()) {
                        final Resource resolved = resource.getFile().isDirectory()
                                ? location.createRelative(path + "/index.html")
                                : resource;
                        if (resolved.isReadable()) {
                            return resolved;
                        }
                    }
                } catch (IOException e) {
                    continue;
                }
            }
            return null;
        }
    }
}
