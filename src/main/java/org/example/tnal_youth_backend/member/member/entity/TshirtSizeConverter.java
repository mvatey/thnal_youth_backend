package org.example.tnal_youth_backend.member.member.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists the API/display value of TshirtSize (for example "2XL")
 * instead of the Java enum constant name (for example "TWO_XL").
 */
@Converter
public class TshirtSizeConverter implements AttributeConverter<TshirtSize, String> {

    @Override
    public String convertToDatabaseColumn(TshirtSize attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public TshirtSize convertToEntityAttribute(String dbData) {
        return TshirtSize.fromValue(dbData);
    }
}
