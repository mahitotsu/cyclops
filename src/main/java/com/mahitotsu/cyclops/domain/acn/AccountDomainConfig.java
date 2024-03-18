package com.mahitotsu.cyclops.domain.acn;

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
public class AccountDomainConfig {

    @Bean
    @Qualifier("acn")
    public LocalContainerEntityManagerFactoryBean acnPu(final EntityManagerFactoryBuilder builder,
            final DataSource dataSource) {
        return builder.dataSource(dataSource).persistenceUnit("ACN-PU")
                .packages(this.getClass().getPackage().getName()).build();
    }

    @Bean
    public PlatformTransactionManager acnTxMgr(@NonNull @Qualifier("acn") final EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}