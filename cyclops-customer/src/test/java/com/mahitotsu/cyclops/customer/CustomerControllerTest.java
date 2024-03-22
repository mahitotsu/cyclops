package com.mahitotsu.cyclops.customer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import com.mahitotsu.cyclops.customer.CustomerControllerTest.TestComponents;
import com.mahitotsu.cyclops.service.ApiClientUtils;
import com.mahitotsu.cyclops.test.TestContainersUtils;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = """
       spring.jpa.open-in-view=false 
       spring.jpa.generate-ddl=true
       spring.jpa.show-sql=false
        """)
@ContextConfiguration(classes = { TestComponents.class })
public class CustomerControllerTest {

    @TestConfiguration
    public static class TestComponents {

        @Bean
        @ServiceConnection("postgres")
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return TestContainersUtils.postgreSQLContainer();
        }
    }

    @LocalServerPort
    private int localServerPort;

    private CustomerApi client;

    @BeforeEach
    public void setup() {
        this.client = ApiClientUtils.createHttpServiceProxy(CustomerApi.class, "localhost", this.localServerPort);
    }

    @Test
    public void test_curdCustomer() {

        // create
        final CustomerId customerId = this.client.registerNewCustomer();
        assertNotNull(customerId);

        // refere
        final CustomerInfo customerInfo = this.client.describeCustomer(customerId);
        assertNotNull(customerInfo);
        assertEquals(customerId, customerInfo.getId());

        // update
        // --- no such operation

        // delete
        this.client.deactivateCustomer(customerId);
        assertNull(this.client.describeCustomer(customerId));
    }
}
