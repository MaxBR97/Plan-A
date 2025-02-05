package DTO.Records.Model.ModelData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record InputDTO(
        Map<String, List<List<String>>> setsToValues,
        Map<String, List<String>> paramsToValues,
        List<String> constraintsToggledOff,
        List<String> preferencesToggledOff
    ) {}