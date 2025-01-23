package DTO.Records.Image;

import DTO.Records.Model.ModelDefinition.VariableDTO;

import java.util.List;
import java.util.Map;

/**
 *
 * @param constraints A map from module names to their DTO object
 * @param preferences A map from module names to their DTO object
 */
public record ImageDTO(String imageId,
                       VariableModuleDTO variablesModule,
                       List<ConstraintModuleDTO> constraintModules,
                       List<PreferenceModuleDTO> preferenceModules
                       )
{}
