package DTO.Records.Model.ModelDefinition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VariableDTO(
    @NotBlank String identifier,
    List<String> tags,
    List<String> type,
    @Valid @NotNull DependenciesDTO dep
) {}
