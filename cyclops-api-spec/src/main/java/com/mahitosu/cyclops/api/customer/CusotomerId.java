package com.mahitosu.cyclops.api.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode
@ToString
@Getter(AccessLevel.NONE)
public class CusotomerId {
    @NotNull
    @Pattern(regexp = "[0-9]{8}")
    private final String value;
}
