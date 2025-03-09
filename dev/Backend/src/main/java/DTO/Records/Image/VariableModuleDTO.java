package DTO.Records.Image;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record VariableModuleDTO(@Valid Set<@NotBlank String> variablesOfInterest,
                                @Valid Set<@NotBlank String> inputSets,
                                @Valid Set<@NotBlank String> inputParams
                                ) {
    
}
