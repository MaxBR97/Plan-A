package DTO.Records.Model.ModelDefinition;

import java.util.List;

public record PreferenceDTO(
    String identifier,
    DependenciesDTO dep
) {}