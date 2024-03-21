package com.mahitotsu.cyclops.customer.api;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(value = "/api")
public interface CustomerApi {

    static interface Server extends CustomerApi {
    }

    static interface Client extends CustomerApi {
    }

    @PostExchange(value = "/register-new-customer")
    CustomerId registerNewCustomer();

    @PostExchange(value = "/deactivate-customer")
    void deactivateCustomer(CustomerId id);

    @PostExchange(value = "/describe-customer")
    CustomerInfo describeCustomer(CustomerId id);
}
