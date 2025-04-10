package DTO.Records.Model.ModelDefinition;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record VariableDTO(
    @NotBlank String identifier,
    List<String> tags,
    List<String> type,
    @Valid 
    @JsonInclude(JsonInclude.Include.NON_NULL)
    DependenciesDTO dep,
    String boundSet
) {}
