package DTO.Records.Image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolutionValueDTO(@NotNull @Valid List<@NotNull String> values, @NotNull int objectiveValue) {
}
