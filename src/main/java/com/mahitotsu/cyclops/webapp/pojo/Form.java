package com.mahitotsu.cyclops.webapp.pojo;

import lombok.Data;

@Data
public class Form {

    public static String getFormat(final Class<? extends Form> formType) {
        try {
            return formType.getField("FORMAT").get(null).toString();
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Can not determine the format name.", e);
        }
    }

    private String referenceNumber;
}
