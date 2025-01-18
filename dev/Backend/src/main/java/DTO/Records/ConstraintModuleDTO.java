package DTO.Records;

import java.util.Map;

public record ConstraintModuleDTO(boolean isActive, String name, String description, Map<String,ConstraintDTO> module) {
}
