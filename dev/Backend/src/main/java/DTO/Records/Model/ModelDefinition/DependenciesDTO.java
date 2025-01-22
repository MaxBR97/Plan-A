package DTO.Records.Model.ModelDefinition;


import java.util.List;

public record DependenciesDTO(
    List<String> setDependencies,
    List<String> paramDependencies
) {}
