package DTO.Records.Image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public record SolutionVariable(@Valid @NotNull List<@NotNull String> setStructure,@Valid @NotNull List<@NotNull String> typeStructure,
                               @Valid @NotNull Set<@Valid @NotNull SolutionValueDTO> solutions) {
}
