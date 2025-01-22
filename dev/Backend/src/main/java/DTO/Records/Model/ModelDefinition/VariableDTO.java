package DTO.Records.Model.ModelDefinition;

import java.util.List;

public record VariableDTO(
    String name,
    DependenciesDTO dep
) {}
