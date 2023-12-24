package com.mahitotsu.cyclops.webapp.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.mahitotsu.cyclops.webapp.TestContainerConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@DataJpaTest(properties = """
        spring.jpa.generate-ddl=true
        """, showSql = true)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ TestContainerConfiguration.class, EntityConfiguration.class })
public @interface EntityTest {
    
}
