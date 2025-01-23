package DTO.Records.Image;

import java.util.List;

import DTO.Records.Model.ModelDefinition.VariableDTO;

public record VariableModuleDTO(List<String> variablesOfInterest,
                                List<String> variablesConfigurableSets,
                                List<String> variablesConfigurableParams
                                ) {
    
}
