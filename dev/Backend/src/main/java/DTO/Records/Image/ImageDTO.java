package DTO.Records.Image;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import Exceptions.BadRequestException;
public record ImageDTO(
    @NotNull @Valid String imageId,
    String imageName,
    String imageDescription,
    String owner,
    Boolean isPrivate,
    Map<String, String> solverSettings,
    VariableModuleDTO variablesModule,
    Set<ConstraintModuleDTO> constraintModules,
    Set<PreferenceModuleDTO> preferenceModules,
    Boolean isConfigured
) {
    // Overloaded constructor that sets isConfigured to false
    public ImageDTO(
        @NotNull @Valid String imageId,
        String imageName,
        String imageDescription,
        String owner,
        Boolean isPrivate,
        Map<String, String> solverSettings,
        VariableModuleDTO variablesModule,
        Set<ConstraintModuleDTO> constraintModules,
        Set<PreferenceModuleDTO> preferenceModules
    ) {
        this(
            imageId,
            imageName,
            imageDescription,
            owner,
            isPrivate,
            solverSettings,
            variablesModule,
            constraintModules,
            preferenceModules,
            false // default value
        );
    }

    public void validateNoDuplicateVariableTags() throws BadRequestException {
        if (variablesModule == null || variablesModule.variablesOfInterest() == null) {
            return;
        }

        for (VariableDTO variable : variablesModule.variablesOfInterest()) {
            if (variable.tags() == null) {
                continue;
            }

            Set<String> seenTags = new HashSet<>();
            for (String tag : variable.tags()) {
                if (tag == null || tag.trim().isEmpty()) {
                    throw new BadRequestException(
                        String.format("Empty tag found in variable '%s'",
                            variable.identifier())
                    );
                }

                if (!seenTags.add(tag)) {
                    throw new BadRequestException(
                        String.format("Duplicate tag found in variable '%s': '%s'",
                            variable.identifier(),
                            tag)
                    );
                }
            }
        }
    }
}
