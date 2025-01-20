package DTO.Records.Model.ModelDefinition;

import DTO.Records.Model.ModelData.SetDefinitionDTO;
import java.util.Collection;

public record VariableDTO(String name, boolean isComplex, Collection<SetDefinitionDTO> dependencies) {
}
