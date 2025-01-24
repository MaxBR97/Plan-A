package DTO.Records.Image;

import java.util.Set;

import DTO.Records.Model.ModelDefinition.VariableDTO;

public record VariableModuleDTO(Set<String> variablesOfInterest,
                                Set<String> variablesConfigurableSets,
                                Set<String> variablesConfigurableParams
                                ) {
    
}
