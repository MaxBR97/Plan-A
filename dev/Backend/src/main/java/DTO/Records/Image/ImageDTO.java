package DTO.Records.Image;

import DTO.Records.Model.ModelDefinition.VariableDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;


public record ImageDTO(
                       @NotNull VariableModuleDTO variablesModule,
                       @NotNull Set<ConstraintModuleDTO> constraintModules,
                       @NotNull Set<PreferenceModuleDTO> preferenceModules
                       )
{}
