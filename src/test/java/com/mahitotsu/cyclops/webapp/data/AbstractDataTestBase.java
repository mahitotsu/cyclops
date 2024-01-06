package com.mahitotsu.cyclops.webapp.data;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ DataTestConfiguration.class, DataConfiguration.class })
public class AbstractDataTestBase {

}
