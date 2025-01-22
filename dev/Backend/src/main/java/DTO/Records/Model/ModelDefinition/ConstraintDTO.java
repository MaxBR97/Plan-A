package DTO.Records.Model.ModelDefinition;

import java.util.List;

public record ConstraintDTO(
    String name,
    DependenciesDTO dep
) {}
