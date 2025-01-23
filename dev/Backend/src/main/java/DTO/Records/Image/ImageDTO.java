package DTO.Records.Image;

import DTO.Records.Model.ModelDefinition.VariableDTO;

import java.util.Map;

/**
 *
 * @param constraints A map from module names to their DTO object
 * @param preferences A map from module names to their DTO object
 */
public record ImageDTO(Map<String, ConstraintModuleDTO> constraints,
                       Map<String, PreferenceModuleDTO> preferences,
                       Map<String, VariableDTO> variables)
{}
