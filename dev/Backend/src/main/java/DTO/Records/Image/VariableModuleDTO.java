package DTO.Records.Image;

import java.util.Set;

import DTO.Records.Model.ModelData.ParameterDefinitionDTO;
import DTO.Records.Model.ModelData.SetDefinitionDTO;
import DTO.Records.Model.ModelDefinition.VariableDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record VariableModuleDTO(@Valid Set<@NotNull VariableDTO> variablesOfInterest,
                                @Valid Set<@NotNull SetDefinitionDTO> inputSets,
                                @Valid Set<@NotNull ParameterDefinitionDTO> inputParams
                                ) {
    
}
