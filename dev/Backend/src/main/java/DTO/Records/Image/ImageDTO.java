package DTO.Records.Image;

import DTO.Records.Model.ModelDefinition.VariableDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;


public record ImageDTO(
                       VariableModuleDTO variablesModule,
                       Set<ConstraintModuleDTO> constraintModules,
                       Set<PreferenceModuleDTO> preferenceModules
                       )
{}
