package DTO.Records.Model.ModelDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ModelDTO(
    Set<ConstraintDTO> constraints,
    Set<PreferenceDTO> preferences,
    Set<VariableDTO> variables,
    Map<String, List<String>> setTypes,
    Map<String, String> paramTypes,
    Map<String,List<String>> varTypes

) {}
