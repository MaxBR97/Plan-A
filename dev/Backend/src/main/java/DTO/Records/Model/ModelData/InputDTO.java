package DTO.Records.Model.ModelData;

import java.util.List;
import java.util.Map;

public record InputDTO(
        Map<String, List<String>> setsToValues,
        Map<String, String> paramsToValues,
        List<String> constraintsToggledOff,
        List<String> preferencesToggledOff
    ) {}