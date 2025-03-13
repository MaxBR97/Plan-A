package DTO.Records.Image;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record SolutionDTO(@NotNull boolean solved, @Min(0) double solvingTime,
                            @NotNull String solutionStatus,
                          @NotNull double objectiveValue, @NotNull String errorMsg,
                          @NotNull Map<@NotBlank String, @Valid @NotNull SolutionVariable> solution) {}
