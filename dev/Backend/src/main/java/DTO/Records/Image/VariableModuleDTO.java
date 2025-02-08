package DTO.Records.Image;

import java.util.Set;

import DTO.Records.Model.ModelDefinition.VariableDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record VariableModuleDTO(@Valid Set<@NotBlank String> variablesOfInterest,
                                @Valid Set<@NotBlank String> variablesConfigurableSets,
                                @Valid Set<@NotBlank String> variablesConfigurableParams
                                ) {
    
}
