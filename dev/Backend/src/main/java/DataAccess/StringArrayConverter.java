package DataAccess;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    private static final String DELIMITER = ","; // Choose a delimiter

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return Arrays.stream(attribute).collect(Collectors.joining(DELIMITER));
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new String[0];
        }
        return dbData.split(DELIMITER);
    }
}