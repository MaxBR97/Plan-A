package DTO.Records.Image;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;


/**
 *
 *
 */
public record ConstraintModuleDTO(@NotBlank String moduleName, @NotNull String description, @Valid Set<@NotBlank String> constraints,
                                  @Valid Set<SetDefinitionDTO> inputSets,
                                  @Valid Set<ParameterDefinitionDTO> inputParams) {
                                    public ConstraintModuleDTO {
                                        if (description == null) {
                                            description = "";
                                        }
                                    }
                                  }
