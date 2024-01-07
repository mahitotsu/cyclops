package com.mahitotsu.cyclops.webapp.data;

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

import com.mahitotsu.cyclops.webapp.data.validation.ForCreate;
import com.mahitotsu.cyclops.webapp.data.validation.ForDelete;
import com.mahitotsu.cyclops.webapp.data.validation.ForUpdate;

import jakarta.validation.groups.Default;

@Configuration
@EnableJpaAuditing(modifyOnCreate = true, auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
public class DataConfiguration implements BeanPostProcessor {

    @Bean(name = "dateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(TransactionSynchronizationManager.getCurrentTransactionName());
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
}