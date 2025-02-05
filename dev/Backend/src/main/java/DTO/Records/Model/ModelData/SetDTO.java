package DTO.Records.Model.ModelData;

import java.util.Collection;
import java.util.List;

/**
 *
 * @param setDefinition The set's definition: name, type, etc.
 * @param values The set's actual value (only applicable for non composition sets)
 */
public record SetDTO(SetDefinitionDTO setDefinition, List<String> values) { }
