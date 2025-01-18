package DTO.Records;

import java.util.Map;

public record ImageDTO(Map<String, ConstraintModuleDTO> constraints,
                       Map<String, PreferenceModuleDTO> preferences)
{ }
