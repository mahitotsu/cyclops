package com.mahitotsu.cyclops.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    @Transactional
    @Valid
    public CustomerId registerNewCustomer() {

        final CustomerEntity entity = new CustomerEntity();
        entity.setActive(true);
        this.repository.save(entity);

        return CustomerId.builder().value(String.format("%08d", entity.getId())).build();
    }

    @Transactional(readOnly = true)
    @Valid
    public CustomerInfo desribCustomerInfo(@Valid final CustomerId customerId) {

        final CustomerEntity entity = this.findCustomerEntity(customerId);
        if (entity == null || entity.isActive() == false) {
            return null;
        }

        return CustomerInfo.builder().id(customerId).active(true).build();
    }

    @Transactional
    public void deactivateCustomer(@Valid final CustomerId customerId) {

        final CustomerEntity entity = this.findCustomerEntity(customerId);
        if (entity != null) {
            entity.setActive(false);
        }
    }

    private CustomerEntity findCustomerEntity(final CustomerId customerId) {
        return customerId == null ? null : this.repository.findById(Long.parseLong(customerId.getValue())).orElse(null);
    }
}
