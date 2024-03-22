package com.mahitotsu.cyclops.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@RestController
public class CustomerController implements CustomerApi {

    private final Random random = new Random();

    private final Map<CustomerId, CustomerInfo> customers = new HashMap<>();

    @Autowired
    private Validator validator;

    @Override
    public CustomerId registerNewCustomer() {

        final CustomerId customerId = CustomerId.builder()
                .value(String.format("%08d", Double.valueOf(random.nextDouble() * 100000000).intValue())).build();
        final CustomerInfo customerInfo = CustomerInfo.builder().id(customerId).active(true).build();

        final Set<ConstraintViolation<CustomerInfo>> violations = this.validator.validate(customerInfo);
        if (violations.isEmpty() == false) {
            throw new ConstraintViolationException(violations);
        }

        this.customers.put(customerId, customerInfo);
        return customerId;
    }

    @Override
    public void deactivateCustomer(final CustomerId id) {

        final CustomerInfo customerInfo = this.customers.get(id);
        if (customerInfo != null) {
            final CustomerInfo newCustomerInfo = customerInfo.toBuilder().active(false).build();
            this.customers.put(id, newCustomerInfo);
        }
        return;
    }

    @Override
    public CustomerInfo describeCustomer(final CustomerId id) {

        final CustomerInfo customerInfo = this.customers.get(id);
        return customerInfo != null && customerInfo.isActive() ? customerInfo : null;
    }
}
