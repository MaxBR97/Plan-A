package Model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ModelTypeConverter implements AttributeConverter<ModelType, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ModelType attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing ModelType", e);
        }
    }

    
    @Override
public ModelType convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
        return null;
    }
    try {
        dbData = dbData.replace("\"", ""); // Remove unnecessary quotes

        if (dbData.startsWith("{") && dbData.contains("types")) {
            // Convert "{types:[TEXT,TEXT,INT]}" to proper JSON: {"types":["TEXT","TEXT","INT"]}
            dbData = dbData.replace("types:[", "\"types\":[\"")
                           .replace(",", "\",\"")
                           .replace("]", "\"]");

            return objectMapper.readValue(dbData, Tuple.class);
        } else {
            // Deserialize as a single ModelPrimitives enum
            return ModelPrimitives.valueOf(dbData);
        }
    } catch (Exception e) {
        throw new RuntimeException("Error deserializing ModelType: Invalid value - " + dbData, e);
    }
}

}
