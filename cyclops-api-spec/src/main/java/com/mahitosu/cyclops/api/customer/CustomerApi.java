package com.mahitosu.cyclops.api.customer;

import com.mahitosu.cyclops.api.ApiOperation;

public interface CustomerApi {

    @ApiOperation
    CusotomerId registerNewCustomer();

    @ApiOperation
    void deactivateCustomer(CusotomerId id);

    @ApiOperation
    void describeCustomer(CusotomerId id);
}
