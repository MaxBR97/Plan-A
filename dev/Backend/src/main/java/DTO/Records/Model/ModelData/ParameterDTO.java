package DTO.Records.Model.ModelData;

/**
 *
 * @param parameterDefinition The parameter's definition (name, type etc.)
 * @param value The parameter's actual value
 */
public record ParameterDTO(ParameterDefinitionDTO parameterDefinition, String value) {
}
