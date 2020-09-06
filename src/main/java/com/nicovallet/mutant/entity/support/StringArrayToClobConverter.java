package com.nicovallet.mutant.entity.support;

import javax.persistence.AttributeConverter;
import java.util.Arrays;

public class StringArrayToClobConverter implements AttributeConverter<String[], String> {

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        return Arrays.toString(attribute);
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        return dbData.substring(1, dbData.length() - 1).split(", ");
    }
}