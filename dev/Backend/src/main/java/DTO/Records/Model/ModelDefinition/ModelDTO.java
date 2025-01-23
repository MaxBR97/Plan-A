package DTO.Records.Model.ModelDefinition;

import java.util.List;
import java.util.Map;

public record ModelDTO(
    List<ConstraintDTO> constraints, 
    List<PreferenceDTO> preferences, 
    List<VariableDTO> variables,
    Map<String, String> types
) {}
