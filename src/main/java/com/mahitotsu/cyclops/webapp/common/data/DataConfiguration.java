package com.mahitotsu.cyclops.webapp.common.data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahitotsu.cyclops.webapp.common.data.validation.ForCreate;
import com.mahitotsu.cyclops.webapp.common.data.validation.ForDelete;
import com.mahitotsu.cyclops.webapp.common.data.validation.ForUpdate;

import jakarta.validation.groups.Default;

@Configuration
@EnableJpaAuditing(modifyOnCreate = true, auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
public class DataConfiguration implements BeanPostProcessor {

    private static ObjectMapper managedObjectMapper;

    public static Optional<ObjectMapper> getManagedObjectMapper() {
        return Optional.ofNullable(DataConfiguration.managedObjectMapper);
    }

    @Bean(name = "dateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

    @Bean(name = "auditorAware")
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        if (AbstractEntityManagerFactoryBean.class.isInstance(bean) == false) {
            return bean;
        }

        final AbstractEntityManagerFactoryBean factoryBean = AbstractEntityManagerFactoryBean.class.cast(bean);
        final Map<String, Object> jpaProperties = factoryBean.getJpaPropertyMap();
        jpaProperties.put("javax.persistence.validation.group.pre-persist",
                Arrays.asList(Default.class, ForCreate.class).stream().map(c -> c.getCanonicalName())
                        .collect(Collectors.joining(",")));
        jpaProperties.put("javax.persistence.validation.group.pre-update",
                Arrays.asList(Default.class, ForUpdate.class).stream().map(c -> c.getCanonicalName())
                        .collect(Collectors.joining(",")));
        jpaProperties.put("javax.persistence.validation.group.pre-delete",
                Arrays.asList(Default.class, ForDelete.class).stream().map(c -> c.getCanonicalName())
                        .collect(Collectors.joining(",")));
        return factoryBean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (ObjectMapper.class.isInstance(bean)) {
            Assert.isNull(DataConfiguration.managedObjectMapper,
                    "Attempted to initialize a field but it was already set.");
            DataConfiguration.managedObjectMapper = ObjectMapper.class.cast(bean);
        }
        return bean;
    }
}