package DTO.Records.Image;

import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public record ImageDTO(
                       @NotNull @Valid String imageId,
                       @NotNull @Valid String imageName,
                       @NotNull @Valid String imageDescription,
                       String owner,
                       Boolean isPrivate,
                       Map<String, String> solverSettings,
                       @NotNull @Valid VariableModuleDTO variablesModule,
                       @NotNull @Valid Set<ConstraintModuleDTO> constraintModules,
                       @NotNull @Valid Set<PreferenceModuleDTO> preferenceModules
                       )
{}
