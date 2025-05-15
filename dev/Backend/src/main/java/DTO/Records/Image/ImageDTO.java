package DTO.Records.Image;

import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public record ImageDTO(
                       @NotNull @Valid String imageId,
                       String imageName,
                       String imageDescription,
                       String owner,
                       Boolean isPrivate,
                       Map<String, String> solverSettings,
                       VariableModuleDTO variablesModule,
                       Set<ConstraintModuleDTO> constraintModules,
                       Set<PreferenceModuleDTO> preferenceModules
                       )
{}
