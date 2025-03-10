package DTO.Records.Model.ModelDefinition;


import java.util.Set;

import jakarta.validation.Valid;

public record DependenciesDTO(
    @Valid Set<String> setDependencies,
    @Valid Set<String> paramDependencies
) {}
