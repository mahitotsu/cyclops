package com.mahitotsu.cyclops.webapp.data;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
@EnableJpaAuditing(modifyOnCreate = true, dateTimeProviderRef = DataConfiguration.DATETIME_PROVIDER_NAME, auditorAwareRef = DataConfiguration.AUDITOR_AWARE_NAME)
public class DataConfiguration {

    public static final String DATETIME_PROVIDER_NAME = "dateTimeProvider";

    public static final String AUDITOR_AWARE_NAME = "auditorAware";

    @Bean(name = DATETIME_PROVIDER_NAME)
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.ofNullable(LocalDateTime.now());
    }

    @Bean(name = AUDITOR_AWARE_NAME)
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(TransactionSynchronizationManager.getCurrentTransactionName());
    }
}
