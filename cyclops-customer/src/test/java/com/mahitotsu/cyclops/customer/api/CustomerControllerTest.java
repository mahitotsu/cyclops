package com.mahitotsu.cyclops.customer.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class CustomerControllerTest {

    @Autowired
    private CustomerApi.Client client;


    @Test
    public void test_registerNewCustomer() {

        final CustomerId customerId = this.client.registerNewCustomer();
        assertNotNull(customerId);
    }
}
