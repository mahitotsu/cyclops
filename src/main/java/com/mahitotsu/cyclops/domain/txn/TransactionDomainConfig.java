package com.mahitotsu.cyclops.domain.txn;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class TransactionDomainConfig {

    @Bean
    @Qualifier("txn")
    public LocalContainerEntityManagerFactoryBean txnPu(final EntityManagerFactoryBuilder builder,
            final DataSource dataSource) {
        return builder.dataSource(dataSource).persistenceUnit("TXN-PU")
                .packages(this.getClass().getPackage().getName()).build();
    }

    @Bean
    public PlatformTransactionManager txnTxrMgr(@NonNull @Qualifier("txn") final EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}