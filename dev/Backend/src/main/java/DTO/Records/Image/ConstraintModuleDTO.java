package DTO.Records.Image;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;


/**
 *
 *
 */
public record ConstraintModuleDTO(@NotBlank String moduleName, @NotNull String description, @Valid Set<@NotBlank String> constraints,
                                  @Valid Set<@NotBlank String> inputSets,
                                  @Valid Set<@NotBlank String> inputParams) {}
