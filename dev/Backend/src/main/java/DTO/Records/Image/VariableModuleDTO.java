package DTO.Records.Image;

import java.util.Set;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import jakarta.validation.Valid;

public record VariableModuleDTO(@Valid Set<VariableDTO> variablesOfInterest,
                                @Valid Set<SetDefinitionDTO> inputSets,
                                @Valid Set<ParameterDefinitionDTO> inputParams
                                ) {
    
}
