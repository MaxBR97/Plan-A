package DTO.Records.Image;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SolutionValueDTO(@NotNull @Valid List<@NotNull String> values, @NotNull double objectiveValue) {
}
