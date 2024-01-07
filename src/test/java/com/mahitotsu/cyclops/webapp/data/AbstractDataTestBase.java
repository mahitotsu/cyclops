package com.mahitotsu.cyclops.webapp.data;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = """
        spring.jpa.generate-ddl=true
        spring.jpa.show-sql=true
        """)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ DataTestConfiguration.class})
public class AbstractDataTestBase {

}
