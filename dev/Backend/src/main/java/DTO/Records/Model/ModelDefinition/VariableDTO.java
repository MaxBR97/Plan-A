package DTO.Records.Model.ModelDefinition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VariableDTO(
    @NotBlank String identifier,
    @Valid @NotNull DependenciesDTO dep
) {}
