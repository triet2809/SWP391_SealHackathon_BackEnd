package com.fpt.sealhackathon.converter;

import com.fpt.sealhackathon.dto.enums.RegistrationMode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps {@link RegistrationMode} to/from the PostgreSQL {@code registration_mode} column.
 *
 * <p>Required because {@code "new"} is a reserved keyword in Java and cannot be used
 * as an enum constant name. The Java constant is {@code new_team}; the DB value is {@code "new"}.
 * All other values map directly by name.
 */
@Converter(autoApply = true)
public class RegistrationModeConverter implements AttributeConverter<RegistrationMode, String> {

    @Override
    public String convertToDatabaseColumn(RegistrationMode attribute) {
        if (attribute == null) return null;
        // Map new_team → "new"; all others use their declared name
        return attribute == RegistrationMode.new_team ? "new" : attribute.name();
    }

    @Override
    public RegistrationMode convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        if ("new".equals(dbData)) return RegistrationMode.new_team;
        return RegistrationMode.valueOf(dbData);
    }
}