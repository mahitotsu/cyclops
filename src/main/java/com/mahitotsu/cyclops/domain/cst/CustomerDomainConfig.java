package com.mahitotsu.cyclops.domain.cst;

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
public class CustomerDomainConfig {

    @Bean
    @Qualifier("cst")
    public LocalContainerEntityManagerFactoryBean cstPu(final EntityManagerFactoryBuilder builder,
            final DataSource dataSource) {
        return builder.dataSource(dataSource).persistenceUnit("CST-PU")
                .packages(this.getClass().getPackage().getName()).build();
    }

    @Bean
    public PlatformTransactionManager cstTxMgr(@NonNull @Qualifier("cst") final EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}