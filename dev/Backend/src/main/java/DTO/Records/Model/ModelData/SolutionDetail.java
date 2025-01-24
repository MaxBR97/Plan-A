package DTO.Records.Model.ModelData;

import java.util.Map;

public record SolutionDetail(
        Map<String, String> setValues,
        String result
    ) {}