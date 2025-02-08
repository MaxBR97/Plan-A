package DTO.Records.Model.ModelDefinition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConstraintDTO(
        @NotBlank String identifier,
        @NotNull @Valid DependenciesDTO dep
) {}
