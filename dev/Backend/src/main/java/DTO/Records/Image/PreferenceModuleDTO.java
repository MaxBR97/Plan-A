package DTO.Records.Image;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.ConstraintDTO;
import DTO.Records.Model.ModelDefinition.PreferenceDTO;

import java.util.Map;
import java.util.Set;

/**
 *
 * @param isActive if the module is active
 * @param name Module's name
 * @param description Module's description
 * @param preferences A map of preferences names to their DTOs
 * @param setDependencies A set of model Sets of sets which are a part of any preference in this module
 * @param parameterDependencies A set of model parameters of parameters which are a part of any preference in this module
 */
public record PreferenceModuleDTO(boolean isActive, String name, String description, Map<String, PreferenceDTO> preferences,
                                  Set<SetDefinitionDTO> setDependencies,
                                  Set<ParameterDefinitionDTO> parameterDependencies) {
}
