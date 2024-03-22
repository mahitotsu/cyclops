package com.mahitotsu.cyclops.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController implements CustomerApi {

    @Autowired
    private CustomerService service;

    @Override
    public CustomerId registerNewCustomer() {
        return this.service.registerNewCustomer();
    }

    @Override
    public void deactivateCustomer(final CustomerId id) {
        this.service.deactivateCustomer(id);
    }

    @Override
    public CustomerInfo describeCustomer(final CustomerId id) {
        return this.service.desribCustomerInfo(id);
    }
}
