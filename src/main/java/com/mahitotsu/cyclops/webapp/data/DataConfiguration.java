package com.mahitotsu.cyclops.webapp.data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.mahitotsu.cyclops.webapp.data.validation.ForCreate;
import com.mahitotsu.cyclops.webapp.data.validation.ForDelete;
import com.mahitotsu.cyclops.webapp.data.validation.ForUpdate;

import jakarta.validation.groups.Default;

@Configuration
@EnableJpaAuditing(modifyOnCreate = true, dateTimeProviderRef = "dateTimeProvider", auditorAwareRef = "auditorAware")
public class DataConfiguration {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.ofNullable(LocalDateTime.now());
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(TransactionSynchronizationManager.getCurrentTransactionName());
    }

    @Bean
    public BeanPostProcessor emfCustomizer() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
                if (JpaProperties.class.isInstance(bean)) {
                    this.customizeJpaProperties(JpaProperties.class.cast(bean));
                }
                return bean;
            }

            private void customizeJpaProperties(final JpaProperties jpaProperties) {
                final Map<String, String> properties = jpaProperties.getProperties();
                properties.put("javax.persistence.validation.group.pre-persist",
                        String.join(",", Arrays.asList(Default.class.getName(), ForCreate.class.getName())));
                properties.put("javax.persistence.validation.group.pre-update",
                        String.join(",", Arrays.asList(Default.class.getName(), ForUpdate.class.getName())));
                properties.put("javax.persistence.validation.group.pre-remove",
                        String.join(",", Arrays.asList(Default.class.getName(), ForDelete.class.getName())));
            }
        };
    }
}
