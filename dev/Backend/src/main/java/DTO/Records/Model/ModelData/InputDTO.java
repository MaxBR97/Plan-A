package DTO.Records.Model.ModelData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record InputDTO(
        @Valid Map<String, List<List<@NotBlank String>>> setsToValues,
        @Valid Map<String, List<@NotBlank String>> paramsToValues,
        @Valid List<@NotBlank String> constraintModulesToggledOff,
        @Valid List<@NotBlank String> preferenceModulesToggledOff
    ) {}