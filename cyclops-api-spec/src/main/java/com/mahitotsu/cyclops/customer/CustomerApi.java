package com.mahitotsu.cyclops.customer;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(value = "/api")
public interface CustomerApi {

    @PostExchange(value = "/register-new-customer")
    CustomerId registerNewCustomer();

    @PostExchange(value = "/deactivate-customer")
    void deactivateCustomer(@RequestBody CustomerId id);

    @PostExchange(value = "/describe-customer")
    CustomerInfo describeCustomer(@RequestBody CustomerId id);
}
