package DTO.Records.Model.ModelDefinition;


import DTO.Records.Model.ModelData.SetDefinitionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public record DependenciesDTO(
    @NotNull @Valid Set<@NotNull String> setDependencies,
    @NotNull @Valid Set<@NotNull String> paramDependencies
) {}
