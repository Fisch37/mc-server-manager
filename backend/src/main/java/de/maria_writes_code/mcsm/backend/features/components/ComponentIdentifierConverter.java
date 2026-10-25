package de.maria_writes_code.mcsm.backend.features.components;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ComponentIdentifierConverter
        implements AttributeConverter<ComponentIdentifier, String> {

    @Override
    public String convertToDatabaseColumn(ComponentIdentifier attribute) {
        return attribute == null ? null : attribute.id();
    }

    @Override
    public ComponentIdentifier convertToEntityAttribute(String value) {
        return value == null ? null : new ComponentIdentifier(value);
    }
}
