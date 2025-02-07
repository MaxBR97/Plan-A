package DTO.Records.Model.ModelDefinition;


import DTO.Records.Model.ModelData.SetDefinitionDTO;

import java.util.List;
import java.util.Set;

public record DependenciesDTO(
    Set<String> setDependencies,
    Set<String> paramDependencies
) {}
