package DTO.Records.Model.ModelDefinition;

import java.util.List;

public record VariableDTO(
    String identifier,
    DependenciesDTO dep
) {}
